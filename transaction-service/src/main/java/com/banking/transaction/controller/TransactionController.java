package com.banking.transaction.controller;

import com.banking.transaction.dto.TransactionDto;
import com.banking.transaction.model.MoneyRequest;
import com.banking.transaction.model.Transaction;
import com.banking.transaction.model.Wallet;
import com.banking.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // ────────── WALLET ──────────

    // Requirement 2.2: View current wallet balance
    @GetMapping("/api/wallet/balance")
    public ResponseEntity<Wallet> getBalance(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(transactionService.getOrCreateWallet(userId));
    }

    // Internal: credit wallet (used by payment-service)
    @PostMapping("/api/wallet/credit")
    public ResponseEntity<String> creditWallet(@RequestBody TransactionDto.WalletCreditDebitRequest req) {
        transactionService.creditWallet(req.getUserId(), req.getAmount());
        return ResponseEntity.ok("Wallet credited");
    }

    // Internal: debit wallet (used by payment-service)
    @PostMapping("/api/wallet/debit")
    public ResponseEntity<String> debitWallet(@RequestBody TransactionDto.WalletCreditDebitRequest req) {
        transactionService.debitWallet(req.getUserId(), req.getAmount());
        return ResponseEntity.ok("Wallet debited");
    }

    // ────────── TRANSFER ──────────

    // Requirement 3.1: Send money to another customer by phone or username
    @PostMapping("/api/transactions/transfer")
    public ResponseEntity<Transaction> transfer(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TransactionDto.TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(
                userId, request.getReceiverIdentifier(),
                request.getAmount(), request.getDescription()));
    }

    // Requirement 4.1: View own transaction history
    @GetMapping("/api/transactions/history")
    public ResponseEntity<List<Transaction>> getHistory(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(transactionService.getMyTransactions(userId));
    }

    // Admin: all transactions
    @GetMapping("/api/transactions/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    // Admin: search by date range
    @GetMapping("/api/transactions/search")
    public ResponseEntity<List<Transaction>> searchByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(from, to));
    }

    // ────────── MONEY REQUEST ──────────

    // Requirement 5.1: Request money from another customer
    @PostMapping("/api/transactions/request-money")
    public ResponseEntity<MoneyRequest> requestMoney(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TransactionDto.MoneyRequestDto request) {
        return ResponseEntity.ok(transactionService.requestMoney(
                userId, request.getRequesteeId(),
                request.getAmount(), request.getDescription()));
    }

    @PostMapping("/api/transactions/request-money/{requestId}/approve")
    public ResponseEntity<Transaction> approveRequest(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String requestId) {
        return ResponseEntity.ok(transactionService.approveMoneyRequest(requestId, userId));
    }

    @PostMapping("/api/transactions/request-money/{requestId}/reject")
    public ResponseEntity<String> rejectRequest(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String requestId) {
        transactionService.rejectMoneyRequest(requestId, userId);
        return ResponseEntity.ok("Request rejected");
    }

    @GetMapping("/api/transactions/request-money/incoming")
    public ResponseEntity<List<MoneyRequest>> getIncomingRequests(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(transactionService.getIncomingRequests(userId));
    }

    @GetMapping("/api/transactions/request-money/outgoing")
    public ResponseEntity<List<MoneyRequest>> getOutgoingRequests(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(transactionService.getOutgoingRequests(userId));
    }

    // ────────── INTERNAL (used by admin-service via Feign) ──────────

    @GetMapping("/api/transactions/internal/all")
    public ResponseEntity<List<Transaction>> getAllTransactionsInternal() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/api/transactions/internal/search")
    public ResponseEntity<List<Transaction>> searchTransactionsInternal(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (userId != null && from != null && to != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByUserAndDateRange(userId, from, to));
        } else if (userId != null) {
            return ResponseEntity.ok(transactionService.getMyTransactions(userId));
        } else if (from != null && to != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByDateRange(from, to));
        }
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}
