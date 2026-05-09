package com.banking.payment.pattern.strategy;

import com.banking.payment.client.TransactionServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DESIGN PATTERN: Strategy Pattern — Concrete Strategy
 *
 * Handles EXTERNAL PAYMENT (Requirement 9.1):
 *   Debit sender wallet → mock bank transfer to unregistered receiver
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalPaymentStrategy implements PaymentStrategy {

    private final TransactionServiceClient transactionServiceClient;

    @Override
    public PaymentResult execute(String userId, String receiverDetails,
                                 BigDecimal amount, String description) {
        try {
            // Debit sender's wallet
            transactionServiceClient.debitWallet(
                    new TransactionServiceClient.WalletRequest(userId, amount));

            // Mock: send to external bank
            log.info("[EXTERNAL-STRATEGY] Mocking external bank transfer of {} to {}",
                    amount, receiverDetails);

            String refId = "EXT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("[EXTERNAL-STRATEGY] External payment {} succeeded. Ref: {}", amount, refId);

            return PaymentResult.success(refId, amount);

        } catch (Exception e) {
            log.error("[EXTERNAL-STRATEGY] External payment failed: {}", e.getMessage());
            return PaymentResult.failed(e.getMessage());
        }
    }

    @Override
    public String getStrategyName() {
        return "EXTERNAL_PAYMENT";
    }
}
