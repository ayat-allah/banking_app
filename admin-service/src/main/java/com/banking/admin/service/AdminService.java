package com.banking.admin.service;

import com.banking.admin.model.AuditLog;
import com.banking.admin.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AuditLogRepository auditLogRepository;
    private final AuthFeignClient authClient;
    private final TransactionFeignClient transactionClient;
    private final PaymentFeignClient paymentClient;

    // ─────────── USERS ───────────

    // Requirement 10.1: View all registered users
    public List<UserInfo> getAllUsers() {
        return authClient.getAllUsers();
    }

    // Requirement 13.1: Freeze / unfreeze account
    public void freezeUser(String adminId, String adminEmail, String targetUserId, boolean freeze) {
        authClient.updateFreezeStatus(targetUserId, freeze);
        saveAuditLog(adminId, adminEmail,
                freeze ? "FREEZE_ACCOUNT" : "UNFREEZE_ACCOUNT",
                targetUserId,
                (freeze ? "Froze" : "Unfroze") + " account for userId=" + targetUserId);
    }

    // ─────────── TRANSACTIONS ───────────

    // Requirement 11.1: View all internal transfers
    public List<TransactionInfo> getAllInternalTransactions() {
        return transactionClient.getAllTransactions();
    }

    // Requirement 14.1: Search by username (userId) or date range
    public List<TransactionInfo> searchTransactions(String userId, LocalDateTime from, LocalDateTime to) {
        return transactionClient.searchTransactions(userId, from, to);
    }

    // Requirement 12.1: View all external transfers
    public List<ExternalTransactionInfo> getAllExternalTransactions() {
        return paymentClient.getAllExternalTransactions();
    }

    // ─────────── AUDIT LOGS ───────────

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    public void saveAuditLog(String adminId, String adminEmail,
                              String action, String targetUserId, String details) {
        AuditLog log = AuditLog.builder()
                .adminId(adminId)
                .adminEmail(adminEmail)
                .action(action)
                .targetUserId(targetUserId)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    // ─────────────────────────────────────────────────
    // Inner Feign Clients (declared here to keep files minimal)
    // ─────────────────────────────────────────────────

    @FeignClient(name = "auth-service")
    public interface AuthFeignClient {
        @GetMapping("/api/auth/internal/users")
        List<UserInfo> getAllUsers();

        @GetMapping("/api/auth/internal/users/{userId}")
        UserInfo getUserById(@PathVariable String userId);

        @PutMapping("/api/auth/internal/users/{userId}/freeze")
        void updateFreezeStatus(@PathVariable String userId,
                                @RequestParam boolean freeze);
    }

    @FeignClient(name = "transaction-service")
    public interface TransactionFeignClient {
        @GetMapping("/api/transactions/internal/all")
        List<TransactionInfo> getAllTransactions();

        @GetMapping("/api/transactions/internal/search")
        List<TransactionInfo> searchTransactions(
                @RequestParam(required = false) String userId,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to);
    }

    @FeignClient(name = "payment-service")
    public interface PaymentFeignClient {
        @GetMapping("/api/payments/internal/all")
        List<ExternalTransactionInfo> getAllExternalTransactions();
    }

    // Shared data classes
    @lombok.Data @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class UserInfo {
        private String id, name, email, phoneNumber, role;
        private boolean frozen, active;
    }

    @lombok.Data @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class TransactionInfo {
        private String id, senderId, receiverId, type, status, description;
        private BigDecimal amount;
        private LocalDateTime timestamp;
    }

    @lombok.Data @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ExternalTransactionInfo {
        private String id, userId, type, status, receiverEmail, bankAccountId, description;
        private BigDecimal amount;
        private LocalDateTime createdAt;
    }
}
