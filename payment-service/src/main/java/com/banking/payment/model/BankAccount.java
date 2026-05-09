package com.banking.payment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    // Requirement 6.1: link bank account with card number and bank name
    @Column(nullable = false)
    private String cardNumber;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String accountHolderName;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private LocalDateTime linkedAt = LocalDateTime.now();
}
