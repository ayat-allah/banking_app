package com.banking.transaction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;  // Requirement 3.4: unique transaction ID

    @Column(nullable = false)
    private String senderId;

    @Column(nullable = false)
    private String receiverId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;  // Requirement 3.4: success/failed

    private String description;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();  // Requirement 3.4: timestamp

    public enum TransactionType {
        INTERNAL_TRANSFER,
        MONEY_REQUEST,
        REQUEST_FULFILLMENT,
        WALLET_CREDIT,   // deposit from external bank to wallet
        WALLET_DEBIT     // withdrawal from wallet to external bank
    }

    public enum TransactionStatus {
        SUCCESS, FAILED, PENDING
    }
}
