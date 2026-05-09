package com.banking.payment.ocl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * OCL constraints enforced in the payment-service.
 *
 * [OCL-B2] Deposit and withdrawal amount must be positive.
 * [OCL-B3] Wallet must have enough balance before withdrawal.
 */
@Component
@Slf4j
public class PaymentOCLValidator {

    /**
     * [OCL-B2] Amount must be > 0 for any external operation.
     */
    public void validatePositiveAmount(BigDecimal amount, String operation) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            String msg = String.format(
                "[OCL-B2 VIOLATION] %s amount must be > 0, received: %s", operation, amount);
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        log.debug("[OCL-B2 OK] {} amount is positive: {}", operation, amount);
    }

    /**
     * [OCL-B1] Card number must not already be linked and active for this user.
     */
    public void validateNoDuplicateBankAccount(boolean alreadyExists, String cardNumber) {
        if (alreadyExists) {
            String msg = "[OCL-B1 VIOLATION] Bank account already linked: " +
                         cardNumber.substring(cardNumber.length() - 4);
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        log.debug("[OCL-B1 OK] No duplicate bank account");
    }
}
