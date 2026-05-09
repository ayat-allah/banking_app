package com.banking.payment.pattern.strategy;

import java.math.BigDecimal;

/**
 * DESIGN PATTERN: Strategy Pattern
 *
 * Intent:
 *   Define a family of algorithms (payment methods), encapsulate each one,
 *   and make them interchangeable. The Strategy lets the algorithm vary
 *   independently from the clients that use it.
 *
 * Why used here:
 *   The payment-service supports multiple external payment methods:
 *   deposit from bank, withdrawal to bank, and external transfer.
 *   Each method has different steps and validations.
 *   Using Strategy Pattern allows adding new payment methods
 *   without changing existing code (Open/Closed Principle).
 *
 * Participants:
 *   - PaymentStrategy (this interface)    → Strategy
 *   - DepositStrategy                     → ConcreteStrategy
 *   - WithdrawalStrategy                  → ConcreteStrategy
 *   - ExternalPaymentStrategy             → ConcreteStrategy
 *   - PaymentContext                      → Context
 */
public interface PaymentStrategy {

    /**
     * Execute the payment operation.
     *
     * @param userId        the user initiating the payment
     * @param targetAccount the bank account ID or external receiver
     * @param amount        the amount to transfer
     * @param description   optional description
     * @return PaymentResult containing status and reference ID
     */
    PaymentResult execute(String userId, String targetAccount,
                          BigDecimal amount, String description);

    /**
     * Returns the name of this strategy (used for logging).
     */
    String getStrategyName();
}
