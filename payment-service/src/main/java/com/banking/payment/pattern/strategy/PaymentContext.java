package com.banking.payment.pattern.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * DESIGN PATTERN: Strategy Pattern — Context
 *
 * PaymentContext holds a reference to a PaymentStrategy and
 * delegates the execution to it. The caller sets the strategy
 * at runtime based on the payment type requested.
 *
 * Usage example:
 *   context.setStrategy(depositStrategy);
 *   PaymentResult result = context.executePayment(userId, accountId, amount, desc);
 */
@Component
@Slf4j
public class PaymentContext {

    private PaymentStrategy strategy;

    /** Set the strategy at runtime before calling executePayment */
    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
        log.info("[PAYMENT-CONTEXT] Strategy set to: {}", strategy.getStrategyName());
    }

    /**
     * Execute payment using the currently set strategy.
     * Throws IllegalStateException if no strategy has been set.
     */
    public PaymentResult executePayment(String userId, String target,
                                        BigDecimal amount, String description) {
        if (strategy == null) {
            throw new IllegalStateException("No payment strategy has been set");
        }
        log.info("[PAYMENT-CONTEXT] Executing strategy: {} for user: {}",
                strategy.getStrategyName(), userId);
        return strategy.execute(userId, target, amount, description);
    }
}
