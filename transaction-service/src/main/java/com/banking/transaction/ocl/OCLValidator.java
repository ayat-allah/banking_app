package com.banking.transaction.ocl;

import com.banking.transaction.model.MoneyRequest;
import com.banking.transaction.model.Transaction;
import com.banking.transaction.model.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * ═══════════════════════════════════════════════════════════════════
 * OCL VALIDATOR — Java enforcement of OCL constraints
 * ═══════════════════════════════════════════════════════════════════
 *
 * Each method in this class enforces one or more OCL invariants,
 * pre-conditions, or post-conditions defined in OCLConstraintsDoc.
 *
 * Usage: called from service layer before/after operations.
 * Throws IllegalArgumentException or IllegalStateException on violation.
 */
@Component
@Slf4j
public class OCLValidator {

    // ──────────────────────────────────────────────
    // WALLET CONSTRAINTS
    // ──────────────────────────────────────────────

    /**
     * [OCL-W1] Wallet balance must never be negative.
     *
     * OCL: context Wallet inv NonNegativeBalance: self.balance >= 0
     */
    public void validateWalletBalance(Wallet wallet) {
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            String msg = String.format(
                "[OCL-W1 VIOLATION] Wallet balance for userId=%s is negative: %s",
                wallet.getUserId(), wallet.getBalance());
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        log.debug("[OCL-W1 OK] Wallet balance valid: {}", wallet.getBalance());
    }

    // ──────────────────────────────────────────────
    // TRANSACTION CONSTRAINTS
    // ──────────────────────────────────────────────

    /**
     * [OCL-T1] Transfer amount must be strictly positive.
     *
     * OCL: context Transaction inv PositiveAmount: self.amount > 0
     */
    public void validateTransferAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            String msg = "[OCL-T1 VIOLATION] Transfer amount must be > 0, got: " + amount;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        log.debug("[OCL-T1 OK] Transfer amount valid: {}", amount);
    }

    /**
     * [OCL-T2] Sender and receiver must be different.
     *
     * OCL: context Transaction inv SenderReceiverDifferent:
     *        self.senderId <> self.receiverId
     */
    public void validateSenderReceiverDifferent(String senderId, String receiverId) {
        if (senderId != null && senderId.equals(receiverId)) {
            String msg = "[OCL-T2 VIOLATION] Sender and receiver must be different users. Got: " + senderId;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        log.debug("[OCL-T2 OK] Sender and receiver are different");
    }

    /**
     * [OCL-T4] Pre-condition: sender must have sufficient balance before transfer.
     *
     * OCL: context TransactionService::transfer(...)
     *      pre SufficientBalance:
     *        Wallet.balance >= amount
     */
    public void validateSufficientBalance(Wallet senderWallet, BigDecimal amount) {
        if (senderWallet.getBalance().compareTo(amount) < 0) {
            String msg = String.format(
                "[OCL-T4 VIOLATION] Insufficient balance. Available: %s, Required: %s",
                senderWallet.getBalance(), amount);
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        log.debug("[OCL-T4 OK] Sufficient balance: {} >= {}", senderWallet.getBalance(), amount);
    }

    /**
     * [OCL-T5] Post-condition: verify balances updated correctly after transfer.
     *
     * OCL: post BalanceUpdated:
     *        senderWallet.balance  = senderWallet.balance@pre - amount
     *        receiverWallet.balance = receiverWallet.balance@pre + amount
     */
    public void validateTransferPostCondition(BigDecimal senderBalanceBefore,
                                               BigDecimal receiverBalanceBefore,
                                               Wallet senderWallet,
                                               Wallet receiverWallet,
                                               BigDecimal amount) {
        BigDecimal expectedSender   = senderBalanceBefore.subtract(amount);
        BigDecimal expectedReceiver = receiverBalanceBefore.add(amount);

        if (senderWallet.getBalance().compareTo(expectedSender) != 0) {
            String msg = String.format(
                "[OCL-T5 VIOLATION] Sender balance mismatch. Expected: %s, Got: %s",
                expectedSender, senderWallet.getBalance());
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        if (receiverWallet.getBalance().compareTo(expectedReceiver) != 0) {
            String msg = String.format(
                "[OCL-T5 VIOLATION] Receiver balance mismatch. Expected: %s, Got: %s",
                expectedReceiver, receiverWallet.getBalance());
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        log.debug("[OCL-T5 OK] Transfer balances verified correctly");
    }

    /**
     * [OCL-T6] Total money is conserved (sum of balances constant across a transfer).
     */
    public void validateMoneyConservation(BigDecimal totalBefore, BigDecimal totalAfter) {
        if (totalBefore.compareTo(totalAfter) != 0) {
            String msg = String.format(
                "[OCL-T6 VIOLATION] Money not conserved! Before: %s, After: %s",
                totalBefore, totalAfter);
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        log.debug("[OCL-T6 OK] Money conservation verified");
    }

    // ──────────────────────────────────────────────
    // USER CONSTRAINTS
    // ──────────────────────────────────────────────

    /**
     * [OCL-U4] Frozen account cannot initiate transfers.
     *
     * OCL: context TransactionService::transfer(...)
     *      pre AccountNotFrozen: not User.frozen
     */
    public void validateAccountNotFrozen(boolean isFrozen, String userId) {
        if (isFrozen) {
            String msg = "[OCL-U4 VIOLATION] Account is frozen, cannot transfer. userId=" + userId;
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        log.debug("[OCL-U4 OK] Account is not frozen");
    }

    // ──────────────────────────────────────────────
    // MONEY REQUEST CONSTRAINTS
    // ──────────────────────────────────────────────

    /**
     * [OCL-R1] A user cannot request money from themselves.
     *
     * OCL: context MoneyRequest inv SelfRequestNotAllowed:
     *        self.requesterId <> self.requesteeId
     */
    public void validateMoneyRequest(MoneyRequest request) {
        if (request.getRequesterId().equals(request.getRequesteeId())) {
            String msg = "[OCL-R1 VIOLATION] User cannot request money from themselves";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // [OCL-R2] Amount must be positive
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            String msg = "[OCL-R2 VIOLATION] Money request amount must be > 0";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        log.debug("[OCL-R1,R2 OK] Money request valid");
    }

    /**
     * [OCL-B2] Deposit and withdrawal amount must be positive.
     */
    public void validateExternalAmount(BigDecimal amount, String operation) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            String msg = "[OCL-B2 VIOLATION] " + operation + " amount must be > 0, got: " + amount;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        log.debug("[OCL-B2 OK] {} amount valid: {}", operation, amount);
    }
}
