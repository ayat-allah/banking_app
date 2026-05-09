package com.banking.payment.controller;

import com.banking.payment.model.BankAccount;
import com.banking.payment.model.ExternalTransaction;
import com.banking.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Requirement 6.1: Link bank account
    @PostMapping("/api/bank-accounts/link")
    public ResponseEntity<BankAccount> linkBankAccount(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody LinkBankAccountRequest request) {
        return ResponseEntity.ok(paymentService.linkBankAccount(
                userId, request.getCardNumber(),
                request.getBankName(), request.getAccountHolderName()));
    }

    // Requirement 6.2: List linked bank accounts
    @GetMapping("/api/bank-accounts")
    public ResponseEntity<List<BankAccount>> getLinkedAccounts(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(paymentService.getLinkedAccounts(userId));
    }

    @DeleteMapping("/api/bank-accounts/{accountId}")
    public ResponseEntity<String> unlinkBankAccount(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String accountId) {
        paymentService.unlinkBankAccount(userId, accountId);
        return ResponseEntity.ok("Bank account unlinked");
    }

    // Requirement 7.1: Deposit from bank to wallet
    @PostMapping("/api/payments/deposit")
    public ResponseEntity<ExternalTransaction> deposit(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(paymentService.deposit(
                userId, request.getBankAccountId(), request.getAmount()));
    }

    // Requirement 8.1: Withdraw from wallet to bank
    @PostMapping("/api/payments/withdraw")
    public ResponseEntity<ExternalTransaction> withdraw(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(paymentService.withdraw(
                userId, request.getBankAccountId(), request.getAmount()));
    }

    // Requirement 9.1: Send to external user
    @PostMapping("/api/payments/send-external")
    public ResponseEntity<ExternalTransaction> sendToExternal(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ExternalPaymentRequest request) {
        return ResponseEntity.ok(paymentService.sendToExternal(
                userId, request.getReceiverEmail(),
                request.getReceiverBankAccount(),
                request.getAmount(), request.getDescription()));
    }

    @GetMapping("/api/payments/history")
    public ResponseEntity<List<ExternalTransaction>> getMyHistory(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(paymentService.getMyExternalTransactions(userId));
    }

    // Admin endpoint
    @GetMapping("/api/payments/all")
    public ResponseEntity<List<ExternalTransaction>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllExternalTransactions());
    }

    // Internal endpoint used by admin-service via Feign
    @GetMapping("/api/payments/internal/all")
    public ResponseEntity<List<ExternalTransaction>> getAllPaymentsInternal() {
        return ResponseEntity.ok(paymentService.getAllExternalTransactions());
    }

    // ─── Request bodies ───

    @Data @NoArgsConstructor @AllArgsConstructor
    static class LinkBankAccountRequest {
        @NotBlank private String cardNumber;
        @NotBlank private String bankName;
        @NotBlank private String accountHolderName;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    static class TransferRequest {
        @NotBlank private String bankAccountId;
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    static class ExternalPaymentRequest {
        @NotBlank private String receiverEmail;
        @NotBlank private String receiverBankAccount;
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
        private String description;
    }
}
