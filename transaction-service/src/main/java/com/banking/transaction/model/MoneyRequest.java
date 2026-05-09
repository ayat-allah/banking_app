package com.banking.transaction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "money_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String requesterId;   // who is asking for money

    @Column(nullable = false)
    private String requesteeId;   // who should pay

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
