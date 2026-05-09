package com.banking.admin.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// ─────────────────────────────────────────────────
// Auth Service Client
// ─────────────────────────────────────────────────
@FeignClient(name = "auth-service")
interface AuthClient {
    @GetMapping("/api/auth/internal/users")
    List<UserInfo> getAllUsers();

    @GetMapping("/api/auth/internal/users/{userId}")
    UserInfo getUserById(@PathVariable String userId);

    @PutMapping("/api/auth/internal/users/{userId}/freeze")
    void updateFreezeStatus(@PathVariable String userId,
                            @RequestParam boolean freeze);
}

// ─────────────────────────────────────────────────
// Transaction Service Client
// ─────────────────────────────────────────────────
@FeignClient(name = "transaction-service")
interface TransactionClient {
    @GetMapping("/api/transactions/internal/all")
    List<TransactionInfo> getAllTransactions();

    @GetMapping("/api/transactions/internal/search")
    List<TransactionInfo> searchTransactions(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to);
}

// ─────────────────────────────────────────────────
// Payment Service Client
// ─────────────────────────────────────────────────
@FeignClient(name = "payment-service")
interface PaymentClient {
    @GetMapping("/api/payments/internal/all")
    List<ExternalTransactionInfo> getAllExternalTransactions();
}

// ─────────────────────────────────────────────────
// Shared DTOs
// ─────────────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
class UserInfo {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private boolean frozen;
    private boolean active;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
class TransactionInfo {
    private String id;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String type;
    private String status;
    private String description;
    private LocalDateTime timestamp;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
class ExternalTransactionInfo {
    private String id;
    private String userId;
    private BigDecimal amount;
    private String type;
    private String status;
    private String receiverEmail;
    private String bankAccountId;
    private String description;
    private LocalDateTime createdAt;
}

// ─────────────────────────────────────────────────
// Public facade used by AdminService
// ─────────────────────────────────────────────────
public class AdminClients {
    public static Class<AuthClient> auth() { return AuthClient.class; }
    public static Class<TransactionClient> transaction() { return TransactionClient.class; }
    public static Class<PaymentClient> payment() { return PaymentClient.class; }
}
