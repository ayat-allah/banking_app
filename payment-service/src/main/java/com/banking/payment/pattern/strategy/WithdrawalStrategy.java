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
 * Handles the WITHDRAWAL operation:
 *   Debit user wallet → mock credit to linked bank account
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawalStrategy implements PaymentStrategy {

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

            // Debit wallet first (throws if insufficient balance)
            transactionServiceClient.debitWallet(
                    new TransactionServiceClient.WalletRequest(userId, amount));

            // Mock bank API call
            log.info("[WITHDRAWAL-STRATEGY] Mocking bank credit of {} to {}",
                    amount, account.getCardNumber());

            String refId = "WDR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("[WITHDRAWAL-STRATEGY] Withdrawal {} succeeded. Ref: {}", amount, refId);

            return PaymentResult.success(refId, amount);

        } catch (Exception e) {
            log.error("[WITHDRAWAL-STRATEGY] Withdrawal failed: {}", e.getMessage());
            return PaymentResult.failed(e.getMessage());
        }
    }

    @Override
    public String getStrategyName() {
        return "WITHDRAWAL";
    }
}
