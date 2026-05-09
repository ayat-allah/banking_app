package com.banking.transaction.repository;

import com.banking.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    // Requirement 4.1: Customer can view their own transaction history
    @Query("SELECT t FROM Transaction t WHERE t.senderId = :userId OR t.receiverId = :userId ORDER BY t.timestamp DESC")
    List<Transaction> findAllByUserId(String userId);

    // For admin: view all transactions
    List<Transaction> findAllByOrderByTimestampDesc();

    // Admin search by date range
    List<Transaction> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime from, LocalDateTime to);

    // Find by sender or receiver with date filter
    @Query("SELECT t FROM Transaction t WHERE (t.senderId = :userId OR t.receiverId = :userId) AND t.timestamp BETWEEN :from AND :to ORDER BY t.timestamp DESC")
    List<Transaction> findByUserIdAndDateRange(String userId, LocalDateTime from, LocalDateTime to);
}
