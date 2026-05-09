package com.banking.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class TransactionDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferRequest {
        @NotBlank(message = "Receiver identifier is required")
        private String receiverIdentifier; // userId, phone, or email

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoneyRequestDto {
        @NotBlank(message = "Requestee ID is required")
        private String requesteeId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletCreditDebitRequest {
        @NotBlank(message = "User ID is required")
        private String userId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;
    }
}
