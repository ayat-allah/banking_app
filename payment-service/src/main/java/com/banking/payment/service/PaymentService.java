package com.banking.payment.service;

import com.banking.payment.client.TransactionServiceClient;
import com.banking.payment.ocl.PaymentOCLValidator;
import com.banking.payment.model.BankAccount;
import com.banking.payment.model.ExternalTransaction;
import com.banking.payment.repository.BankAccountRepository;
import com.banking.payment.repository.ExternalTransactionRepository;
import com.banking.payment.pattern.strategy.DepositStrategy;
import com.banking.payment.pattern.strategy.ExternalPaymentStrategy;
import com.banking.payment.pattern.strategy.PaymentContext;
import com.banking.payment.pattern.strategy.PaymentResult;
import com.banking.payment.pattern.strategy.WithdrawalStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final BankAccountRepository bankAccountRepository;
    private final ExternalTransactionRepository externalTransactionRepository;
    private final TransactionServiceClient transactionServiceClient;

    // DESIGN PATTERN: Strategy — switches payment algorithm at runtime
    private final PaymentContext paymentContext;
    private final DepositStrategy depositStrategy;
    private final WithdrawalStrategy withdrawalStrategy;
    private final ExternalPaymentStrategy externalPaymentStrategy;
    private final PaymentOCLValidator oclValidator; // OCL constraints

    // ─────────── BANK ACCOUNT LINKING ───────────

    // Requirement 6.1: Link a bank account
    @Transactional
    public BankAccount linkBankAccount(String userId, String cardNumber,
                                       String bankName, String accountHolderName) {
        // [OCL-B1] No duplicate linked bank account
        boolean alreadyExists = bankAccountRepository.existsByUserIdAndCardNumber(userId, cardNumber);
        oclValidator.validateNoDuplicateBankAccount(alreadyExists, cardNumber);
        if (alreadyExists) {
            throw new RuntimeException("Bank account already linked");
        }

        BankAccount account = BankAccount.builder()
                .userId(userId)
                .cardNumber(maskCardNumber(cardNumber))
                .bankName(bankName)
                .accountHolderName(accountHolderName)
                .build();

        return bankAccountRepository.save(account);
    }

    // Requirement 6.2: List linked bank accounts
    public List<BankAccount> getLinkedAccounts(String userId) {
        return bankAccountRepository.findByUserIdAndActiveTrue(userId);
    }

    public void unlinkBankAccount(String userId, String accountId) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        account.setActive(false);
        bankAccountRepository.save(account);
    }

    // ─────────── DEPOSIT ───────────

    // Requirement 7.1: Deposit from linked bank to wallet (MOCKED)
    @Transactional
    public ExternalTransaction deposit(String userId, String bankAccountId, BigDecimal amount) {
        oclValidator.validatePositiveAmount(amount, "DEPOSIT"); // [OCL-B2]
        BankAccount account = validateBankAccount(userId, bankAccountId);

        // Mock: in real system this would call bank API
        log.info("[MOCK-BANK] Initiating deposit of {} from bank account {}",
                amount, account.getCardNumber());

        // Credit the wallet via transaction-service
        transactionServiceClient.creditWallet(
                new TransactionServiceClient.WalletRequest(userId, amount));

        // Requirement 7.4: Log external transaction with reference ID
        ExternalTransaction ext = ExternalTransaction.builder()
                .userId(userId)
                .amount(amount)
                .type(ExternalTransaction.ExternalTransactionType.DEPOSIT)
                .bankAccountId(bankAccountId)
                .status(ExternalTransaction.ExternalStatus.SUCCESS)
                .description("Deposit from " + account.getBankName())
                .build();

        return externalTransactionRepository.save(ext);
    }

    // ─────────── WITHDRAW ───────────

    // Requirement 8.1: Withdraw from wallet to linked bank (MOCKED)
    @Transactional
    public ExternalTransaction withdraw(String userId, String bankAccountId, BigDecimal amount) {
        oclValidator.validatePositiveAmount(amount, "WITHDRAWAL"); // [OCL-B2]
        BankAccount account = validateBankAccount(userId, bankAccountId);

        // Debit the wallet (checks balance internally)
        transactionServiceClient.debitWallet(
                new TransactionServiceClient.WalletRequest(userId, amount));

        // Mock: in real system this would call bank API
        log.info("[MOCK-BANK] Initiating withdrawal of {} to bank account {}",
                amount, account.getCardNumber());

        // Requirement 8.4: Log with external reference ID
        ExternalTransaction ext = ExternalTransaction.builder()
                .userId(userId)
                .amount(amount)
                .type(ExternalTransaction.ExternalTransactionType.WITHDRAWAL)
                .bankAccountId(bankAccountId)
                .status(ExternalTransaction.ExternalStatus.SUCCESS)
                .description("Withdrawal to " + account.getBankName())
                .build();

        return externalTransactionRepository.save(ext);
    }

    // ─────────── EXTERNAL PAYMENT ───────────

    // Requirement 9.1: Send to external (non-registered) user
    // DESIGN PATTERN: Strategy — delegates to ExternalPaymentStrategy
    @Transactional
    public ExternalTransaction sendToExternal(String userId, String receiverEmail,
                                               String receiverBankAccount, BigDecimal amount,
                                               String description) {
        // Strategy Pattern: set external payment strategy and execute
        paymentContext.setStrategy(externalPaymentStrategy);
        PaymentResult result = paymentContext.executePayment(
                userId, receiverEmail + "|" + receiverBankAccount, amount, description);

        // Requirement 9.3: Mock bank transfer logged with reference ID
        ExternalTransaction ext = ExternalTransaction.builder()
                .userId(userId)
                .amount(amount)
                .type(ExternalTransaction.ExternalTransactionType.EXTERNAL_PAYMENT)
                .receiverEmail(receiverEmail)
                .receiverBankAccount(receiverBankAccount)
                .status(result.getStatus() == PaymentResult.Status.SUCCESS
                        ? ExternalTransaction.ExternalStatus.SUCCESS
                        : ExternalTransaction.ExternalStatus.FAILED)
                .description((description != null ? description : "External payment to " + receiverEmail)
                        + " [ref=" + result.getReferenceId() + "]")
                .build();

        return externalTransactionRepository.save(ext);
    }

    // ─────────── HISTORY ───────────

    public List<ExternalTransaction> getMyExternalTransactions(String userId) {
        return externalTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<ExternalTransaction> getAllExternalTransactions() {
        return externalTransactionRepository.findAllByOrderByCreatedAtDesc();
    }

    // ─────────── HELPERS ───────────

    private BankAccount validateBankAccount(String userId, String bankAccountId) {
        BankAccount account = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (!account.isActive()) {
            throw new RuntimeException("Bank account is not active");
        }
        return account;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
