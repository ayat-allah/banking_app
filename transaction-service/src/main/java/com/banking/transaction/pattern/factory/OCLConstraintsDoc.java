package com.banking.transaction.pattern.factory;

/**
 * ═══════════════════════════════════════════════════════════════════
 * OBJECT CONSTRAINT LANGUAGE (OCL) — Banking System
 * ═══════════════════════════════════════════════════════════════════
 *
 * OCL is a formal language used to express constraints on UML models.
 * Below are the OCL constraints for the Banking System domain model,
 * followed by their Java enforcement implementation.
 *
 * Context: Banking System — Spring Boot Microservices
 * ═══════════════════════════════════════════════════════════════════
 *
 *
 * ───────────────────────────────────────────
 * 1. WALLET CONSTRAINTS
 * ───────────────────────────────────────────
 *
 * [OCL-W1] Wallet balance must never be negative:
 *   context Wallet
 *   inv NonNegativeBalance:
 *     self.balance >= 0
 *
 * [OCL-W2] Each user has exactly one wallet:
 *   context User
 *   inv OneWalletPerUser:
 *     Wallet.allInstances()->select(w | w.userId = self.id)->size() = 1
 *
 * [OCL-W3] Wallet initial balance is zero:
 *   context Wallet::Wallet()
 *   post InitialBalance:
 *     self.balance = 0
 *
 *
 * ───────────────────────────────────────────
 * 2. TRANSACTION CONSTRAINTS
 * ───────────────────────────────────────────
 *
 * [OCL-T1] Transfer amount must be strictly positive:
 *   context Transaction
 *   inv PositiveAmount:
 *     self.amount > 0
 *
 * [OCL-T2] Sender and receiver must be different users:
 *   context Transaction
 *   inv SenderReceiverDifferent:
 *     self.senderId <> self.receiverId
 *
 * [OCL-T3] Every transaction must have a unique ID and timestamp:
 *   context Transaction
 *   inv HasIdAndTimestamp:
 *     self.id <> null and self.timestamp <> null
 *
 * [OCL-T4] Pre-condition for transfer — sender must have sufficient balance:
 *   context TransactionService::transfer(senderId, receiverId, amount)
 *   pre SufficientBalance:
 *     Wallet.allInstances()
 *       ->any(w | w.userId = senderId).balance >= amount
 *
 * [OCL-T5] Post-condition — sender balance decreases, receiver increases:
 *   context TransactionService::transfer(senderId, receiverId, amount)
 *   post BalanceUpdated:
 *     let senderWallet  = Wallet.allInstances()->any(w | w.userId = senderId)  in
 *     let receiverWallet = Wallet.allInstances()->any(w | w.userId = receiverId) in
 *     senderWallet.balance  = senderWallet.balance@pre  - amount and
 *     receiverWallet.balance = receiverWallet.balance@pre + amount
 *
 * [OCL-T6] Total money is conserved (sum of all balances stays constant):
 *   context TransactionService::transfer(senderId, receiverId, amount)
 *   post MoneyConserved:
 *     Wallet.allInstances().balance->sum() =
 *       Wallet.allInstances().balance@pre->sum()
 *
 *
 * ───────────────────────────────────────────
 * 3. USER / AUTH CONSTRAINTS
 * ───────────────────────────────────────────
 *
 * [OCL-U1] Email must be unique across all users:
 *   context User
 *   inv UniqueEmail:
 *     User.allInstances()->isUnique(u | u.email)
 *
 * [OCL-U2] Phone number must be unique across all users:
 *   context User
 *   inv UniquePhone:
 *     User.allInstances()->isUnique(u | u.phoneNumber)
 *
 * [OCL-U3] Password must not be stored in plain text:
 *   context User
 *   inv PasswordHashed:
 *     self.password.startsWith('$2a$') -- BCrypt prefix
 *
 * [OCL-U4] Frozen account cannot initiate transfers:
 *   context TransactionService::transfer(senderId, receiverId, amount)
 *   pre AccountNotFrozen:
 *     not User.allInstances()->any(u | u.id = senderId).frozen
 *
 * [OCL-U5] Role must be either CUSTOMER or ADMIN:
 *   context User
 *   inv ValidRole:
 *     self.role = Role::CUSTOMER or self.role = Role::ADMIN
 *
 *
 * ───────────────────────────────────────────
 * 4. BANK ACCOUNT CONSTRAINTS
 * ───────────────────────────────────────────
 *
 * [OCL-B1] A user cannot link the same card number twice:
 *   context BankAccount
 *   inv UniqueCardPerUser:
 *     BankAccount.allInstances()
 *       ->select(b | b.userId = self.userId and b.active = true)
 *       ->isUnique(b | b.cardNumber)
 *
 * [OCL-B2] Deposit and withdrawal amount must be positive:
 *   context PaymentService::deposit(userId, bankAccountId, amount)
 *   pre PositiveDepositAmount:
 *     amount > 0
 *
 *   context PaymentService::withdraw(userId, bankAccountId, amount)
 *   pre PositiveWithdrawAmount:
 *     amount > 0
 *
 * [OCL-B3] Withdrawal pre-condition — wallet must have enough balance:
 *   context PaymentService::withdraw(userId, bankAccountId, amount)
 *   pre SufficientWalletBalance:
 *     Wallet.allInstances()->any(w | w.userId = userId).balance >= amount
 *
 *
 * ───────────────────────────────────────────
 * 5. MONEY REQUEST CONSTRAINTS
 * ───────────────────────────────────────────
 *
 * [OCL-R1] A user cannot request money from themselves:
 *   context MoneyRequest
 *   inv SelfRequestNotAllowed:
 *     self.requesterId <> self.requesteeId
 *
 * [OCL-R2] Money request amount must be positive:
 *   context MoneyRequest
 *   inv PositiveRequestAmount:
 *     self.amount > 0
 *
 * [OCL-R3] Approved request leads to successful transaction:
 *   context MoneyRequest::approve()
 *   post ApprovalCreatesTransaction:
 *     self.status = RequestStatus::APPROVED implies
 *       Transaction.allInstances()->exists(t |
 *         t.senderId = self.requesteeId and
 *         t.receiverId = self.requesterId and
 *         t.amount = self.amount and
 *         t.status = TransactionStatus::SUCCESS)
 *
 *
 * ═══════════════════════════════════════════════════════════════════
 * OCL ENFORCEMENT IN JAVA (see OCLValidator class)
 * ═══════════════════════════════════════════════════════════════════
 *
 * The constraints above are enforced programmatically in
 * OCLValidator.java via pre/post condition checks in the service layer.
 *
 */
public class OCLConstraintsDoc {
    // This class is a documentation holder — see OCLValidator for enforcement
    private OCLConstraintsDoc() {}
}
