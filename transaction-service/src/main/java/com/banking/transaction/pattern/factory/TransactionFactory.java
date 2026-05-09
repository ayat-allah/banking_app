package com.banking.transaction.pattern.factory;

import com.banking.transaction.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * DESIGN PATTERN: Factory Pattern
 *
 * Intent:
 *   Define an interface for creating an object, but let subclasses
 *   decide which class to instantiate. Factory Method lets a class
 *   defer instantiation to subclasses.
 *
 * Why used here:
 *   Transaction objects are created in many places with different types
 *   (INTERNAL_TRANSFER, WALLET_CREDIT, WALLET_DEBIT, FAILED).
 *   A Factory centralizes the creation logic, ensures all required fields
 *   are always set correctly, and prevents scattered "new Transaction()"
 *   calls with missing fields (Clean Code + Single Responsibility).
 *
 * Participants:
 *   - TransactionFactory (this class)  → Creator
 *   - Transaction                      → Product
 *   - createTransfer / createFailed    → Factory Methods
 */
@Component
@Slf4j
public class TransactionFactory {

    /**
     * Creates a successful INTERNAL_TRANSFER transaction.
     * Requirement 3.4: generates record with all required fields.
     */
    public Transaction createTransfer(String senderId, String receiverId,
                                       BigDecimal amount, String description) {
        log.info("[FACTORY] Creating INTERNAL_TRANSFER: {} → {} amount={}",
                senderId, receiverId, amount);

        return Transaction.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .amount(amount)
                .type(Transaction.TransactionType.INTERNAL_TRANSFER)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(description != null ? description : "Internal transfer")
                .build();
    }

    /**
     * Creates a WALLET_CREDIT transaction (deposit from external bank).
     */
    public Transaction createWalletCredit(String userId, BigDecimal amount,
                                           String externalRef, String description) {
        log.info("[FACTORY] Creating WALLET_CREDIT for user={} amount={}", userId, amount);

        return Transaction.builder()
                .senderId("EXTERNAL_BANK")
                .receiverId(userId)
                .amount(amount)
                .type(Transaction.TransactionType.WALLET_CREDIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Credit [ref=" + externalRef + "] " + description)
                .build();
    }

    /**
     * Creates a WALLET_DEBIT transaction (withdrawal to external bank).
     */
    public Transaction createWalletDebit(String userId, BigDecimal amount,
                                          String externalRef, String description) {
        log.info("[FACTORY] Creating WALLET_DEBIT for user={} amount={}", userId, amount);

        return Transaction.builder()
                .senderId(userId)
                .receiverId("EXTERNAL_BANK")
                .amount(amount)
                .type(Transaction.TransactionType.WALLET_DEBIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Debit [ref=" + externalRef + "] " + description)
                .build();
    }

    /**
     * Creates a FAILED transaction record.
     * Requirement 3.4: even failed transfers are stored with status=FAILED.
     */
    public Transaction createFailed(String senderId, String receiverId,
                                     BigDecimal amount,
                                     Transaction.TransactionType type,
                                     String reason) {
        log.warn("[FACTORY] Creating FAILED transaction: senderId={} reason={}", senderId, reason);

        return Transaction.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .amount(amount)
                .type(type)
                .status(Transaction.TransactionStatus.FAILED)
                .description("FAILED: " + reason)
                .build();
    }

    /**
     * Creates a MONEY_REQUEST_FULFILLMENT transaction
     * when a money request is approved.
     */
    public Transaction createRequestFulfillment(String payerId, String requesterId,
                                                 BigDecimal amount, String requestId) {
        log.info("[FACTORY] Creating REQUEST_FULFILLMENT: payer={} requester={}", payerId, requesterId);

        return Transaction.builder()
                .senderId(payerId)
                .receiverId(requesterId)
                .amount(amount)
                .type(Transaction.TransactionType.INTERNAL_TRANSFER)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Money request fulfillment [requestId=" + requestId + "]")
                .build();
    }
}
