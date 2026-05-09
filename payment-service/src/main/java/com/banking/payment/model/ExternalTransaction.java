package com.banking.payment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "external_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExternalTransactionType type;

    // Requirement 7.4 / 8.4: external reference ID
    @Builder.Default
    @Column(unique = true)
    private String externalReferenceId = "EXT-" + UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExternalStatus status = ExternalStatus.SUCCESS;

    private String bankAccountId;
    private String receiverEmail;       // for external user payments
    private String receiverBankAccount; // for external user payments

    private String description;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ExternalTransactionType {
        DEPOSIT, WITHDRAWAL, EXTERNAL_PAYMENT
    }

    public enum ExternalStatus {
        SUCCESS, FAILED, PENDING
    }
}
