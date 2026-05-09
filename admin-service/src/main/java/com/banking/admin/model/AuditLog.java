package com.banking.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Requirement 13.2: who, what, when
    @Column(nullable = false)
    private String adminId;

    @Column(nullable = false)
    private String adminEmail;

    @Column(nullable = false)
    private String action;

    private String targetUserId;
    private String details;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
