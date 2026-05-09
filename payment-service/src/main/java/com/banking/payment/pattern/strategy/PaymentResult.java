package com.banking.payment.pattern.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Result returned by any PaymentStrategy execution.
 * Encapsulates outcome data in a uniform structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {

    public enum Status { SUCCESS, FAILED, PENDING }

    private Status status;
    private String referenceId;
    private String message;
    private BigDecimal amount;
    private LocalDateTime processedAt;

    public static PaymentResult success(String referenceId, BigDecimal amount) {
        return PaymentResult.builder()
                .status(Status.SUCCESS)
                .referenceId(referenceId)
                .amount(amount)
                .processedAt(LocalDateTime.now())
                .message("Payment processed successfully")
                .build();
    }

    public static PaymentResult failed(String reason) {
        return PaymentResult.builder()
                .status(Status.FAILED)
                .message(reason)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
