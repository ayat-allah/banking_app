package com.banking.payment.pattern.strategy;

import com.banking.payment.client.TransactionServiceClient;
import com.banking.payment.model.BankAccount;
import com.banking.payment.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DESIGN PATTERN: Strategy Pattern — Concrete Strategy
 *
 * Handles the DEPOSIT operation:
 *   Mock debit from linked bank account → credit user wallet
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DepositStrategy implements PaymentStrategy {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionServiceClient transactionServiceClient;

    @Override
    public PaymentResult execute(String userId, String bankAccountId,
                                 BigDecimal amount, String description) {
        try {
            BankAccount account = bankAccountRepository.findById(bankAccountId)
                    .orElseThrow(() -> new RuntimeException("Bank account not found"));

            if (!account.getUserId().equals(userId)) {
                return PaymentResult.failed("Unauthorized: account does not belong to user");
            }
            if (!account.isActive()) {
                return PaymentResult.failed("Bank account is inactive");
            }

            // Mock bank API call
            log.info("[DEPOSIT-STRATEGY] Mocking bank debit of {} from {}",
                    amount, account.getCardNumber());

            // Credit the wallet
            transactionServiceClient.creditWallet(
                    new TransactionServiceClient.WalletRequest(userId, amount));

            String refId = "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("[DEPOSIT-STRATEGY] Deposit {} succeeded. Ref: {}", amount, refId);

            return PaymentResult.success(refId, amount);

        } catch (Exception e) {
            log.error("[DEPOSIT-STRATEGY] Deposit failed: {}", e.getMessage());
            return PaymentResult.failed(e.getMessage());
        }
    }

    @Override
    public String getStrategyName() {
        return "DEPOSIT";
    }
}
