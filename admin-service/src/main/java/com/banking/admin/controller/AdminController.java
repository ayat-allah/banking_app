package com.banking.admin.controller;

import com.banking.admin.model.AuditLog;
import com.banking.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ─────────── USERS ───────────

    // Requirement 10.1
    @GetMapping("/users")
    public ResponseEntity<List<AdminService.UserInfo>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // Requirement 13.1: Freeze / unfreeze
    @PutMapping("/users/{userId}/freeze")
    public ResponseEntity<Map<String, String>> freezeUser(
            @PathVariable String userId,
            @RequestParam boolean freeze,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-User-Email") String adminEmail) {

        adminService.freezeUser(adminId, adminEmail, userId, freeze);
        String msg = freeze ? "Account frozen successfully" : "Account unfrozen successfully";
        return ResponseEntity.ok(Map.of("message", msg));
    }

    // ─────────── TRANSACTIONS ───────────

    // Requirement 11.1
    @GetMapping("/transactions/internal")
    public ResponseEntity<List<AdminService.TransactionInfo>> getAllInternalTransactions() {
        return ResponseEntity.ok(adminService.getAllInternalTransactions());
    }

    // Requirement 12.1
    @GetMapping("/transactions/external")
    public ResponseEntity<List<AdminService.ExternalTransactionInfo>> getAllExternalTransactions() {
        return ResponseEntity.ok(adminService.getAllExternalTransactions());
    }

    // Requirement 14.1: Search by userId or date range
    @GetMapping("/transactions/search")
    public ResponseEntity<List<AdminService.TransactionInfo>> searchTransactions(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        LocalDateTime fromDate = from != null ? LocalDateTime.parse(from) : null;
        LocalDateTime toDate   = to   != null ? LocalDateTime.parse(to)   : null;
        return ResponseEntity.ok(adminService.searchTransactions(userId, fromDate, toDate));
    }

    // ─────────── AUDIT LOGS ───────────

    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }
}
