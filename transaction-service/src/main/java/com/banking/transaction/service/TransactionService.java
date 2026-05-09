package com.banking.transaction.service;

import com.banking.transaction.client.AuthServiceClient;
import com.banking.transaction.ocl.OCLValidator;
import com.banking.transaction.model.MoneyRequest;
import com.banking.transaction.model.Transaction;
import com.banking.transaction.model.Wallet;
import com.banking.transaction.repository.MoneyRequestRepository;
import com.banking.transaction.repository.TransactionRepository;
import com.banking.transaction.repository.WalletRepository;
import com.banking.transaction.pattern.factory.TransactionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final MoneyRequestRepository moneyRequestRepository;
    private final AuthServiceClient authServiceClient;

    // DESIGN PATTERN: Factory — centralizes Transaction object creation
    private final TransactionFactory transactionFactory;
    private final OCLValidator oclValidator; // OCL enforcement

    // ────────────────────────── WALLET ──────────────────────────

    public Wallet getOrCreateWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder().userId(userId).build()));
    }

    public Wallet getWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    // ──────────────────────── TRANSFER ────────────────────────

    // Requirement 3.1-3.4: Send money - atomic, checks balance, logs transaction
    @Transactional
    public Transaction transfer(String senderUserId, String receiverIdentifier, BigDecimal amount, String description) {
        // OCL pre-conditions
        oclValidator.validateTransferAmount(amount);                               // [OCL-T1]
        oclValidator.validateSenderReceiverDifferent(senderUserId, receiverIdentifier); // [OCL-T2]

        // Check sender is not frozen
        AuthServiceClient.UserInfoResponse sender = authServiceClient.getUserById(senderUserId);
        oclValidator.validateAccountNotFrozen(sender.isFrozen(), senderUserId); // [OCL-U4]
        if (sender.isFrozen()) {
            throw new RuntimeException("Your account is frozen. Contact admin.");
        }

        // Find receiver by phone or username (email used as username)
        AuthServiceClient.UserInfoResponse receiver;
        try {
            receiver = authServiceClient.getUserById(receiverIdentifier);
        } catch (Exception e) {
            throw new RuntimeException("Receiver not found");
        }

        if (receiver.isFrozen()) {
            throw new RuntimeException("Receiver account is frozen");
        }

        Wallet senderWallet = getOrCreateWallet(senderUserId);
        Wallet receiverWallet = getOrCreateWallet(receiver.getId());

        // Requirement 3.2 + OCL-T4: Check sufficient balance
        oclValidator.validateSufficientBalance(senderWallet, amount); // [OCL-T4]
        if (senderWallet.getBalance().compareTo(amount) < 0) {
            transactionRepository.save(transactionFactory.createFailed(
                    senderUserId, receiver.getId(), amount,
                    Transaction.TransactionType.INTERNAL_TRANSFER, "Insufficient balance"));
            throw new RuntimeException("Insufficient balance");
        }

        // Requirement 3.3 + OCL-T5,T6: Transfer is atomic - deduct from sender, add to receiver
        java.math.BigDecimal senderBefore   = senderWallet.getBalance();
        java.math.BigDecimal receiverBefore = receiverWallet.getBalance();
        java.math.BigDecimal totalBefore    = senderBefore.add(receiverBefore);

        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // OCL post-conditions
        oclValidator.validateTransferPostCondition(senderBefore, receiverBefore, senderWallet, receiverWallet, amount); // [OCL-T5]
        oclValidator.validateMoneyConservation(totalBefore, senderWallet.getBalance().add(receiverWallet.getBalance())); // [OCL-T6]
        oclValidator.validateWalletBalance(senderWallet);   // [OCL-W1]
        oclValidator.validateWalletBalance(receiverWallet); // [OCL-W1]

        // Requirement 3.4: Generate transaction record — uses Factory Pattern
        Transaction transaction = transactionFactory.createTransfer(
                senderUserId, receiver.getId(), amount, description);

        return transactionRepository.save(transaction);
    }

    // ──────────────────── MONEY REQUEST ────────────────────

    // Requirement 5.1: Request money from another customer
    @Transactional
    public MoneyRequest requestMoney(String requesterId, String requesteeId, BigDecimal amount, String description) {
        // Validate both users exist
        authServiceClient.getUserById(requesterId);
        authServiceClient.getUserById(requesteeId);

        MoneyRequest request = MoneyRequest.builder()
                .requesterId(requesterId)
                .requesteeId(requesteeId)
                .amount(amount)
                .description(description)
                .build();

        oclValidator.validateMoneyRequest(request); // [OCL-R1, OCL-R2]
        return moneyRequestRepository.save(request);
    }

    @Transactional
    public Transaction approveMoneyRequest(String requestId, String requesteeId) {
        MoneyRequest moneyRequest = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Money request not found"));

        if (!moneyRequest.getRequesteeId().equals(requesteeId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (moneyRequest.getStatus() != MoneyRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request is not pending");
        }

        // Execute the transfer — Factory creates the right transaction type
        Transaction transaction = transfer(
                requesteeId,
                moneyRequest.getRequesterId(),
                moneyRequest.getAmount(),
                "Money request fulfillment [id=" + requestId + "]: " + moneyRequest.getDescription()
        );

        moneyRequest.setStatus(MoneyRequest.RequestStatus.APPROVED);
        moneyRequestRepository.save(moneyRequest);

        return transaction;
    }

    public void rejectMoneyRequest(String requestId, String requesteeId) {
        MoneyRequest moneyRequest = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Money request not found"));

        if (!moneyRequest.getRequesteeId().equals(requesteeId)) {
            throw new RuntimeException("Unauthorized");
        }

        moneyRequest.setStatus(MoneyRequest.RequestStatus.REJECTED);
        moneyRequestRepository.save(moneyRequest);
    }

    // ──────────────────── HISTORY ────────────────────

    // Requirement 4.1: View own transaction history
    public List<Transaction> getMyTransactions(String userId) {
        return transactionRepository.findAllByUserId(userId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByTimestampDesc();
    }

    public List<Transaction> getTransactionsByDateRange(LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByTimestampBetweenOrderByTimestampDesc(from, to);
    }

    public List<Transaction> getTransactionsByUserAndDateRange(String userId, LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByUserIdAndDateRange(userId, from, to);
    }

    public List<MoneyRequest> getIncomingRequests(String userId) {
        return moneyRequestRepository.findByRequesteeIdOrderByCreatedAtDesc(userId);
    }

    public List<MoneyRequest> getOutgoingRequests(String userId) {
        return moneyRequestRepository.findByRequesterIdOrderByCreatedAtDesc(userId);
    }

    // ──────────────────── HELPERS ────────────────────

    // Called internally by payment-service to credit/debit wallet
    @Transactional
    public void creditWallet(String userId, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void debitWallet(String userId, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    // Factory Pattern handles all Transaction creation — no manual builders needed here
}
