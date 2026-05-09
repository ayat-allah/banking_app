package com.banking.payment.repository;

import com.banking.payment.model.ExternalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalTransactionRepository extends JpaRepository<ExternalTransaction, String> {
    List<ExternalTransaction> findByUserIdOrderByCreatedAtDesc(String userId);
    List<ExternalTransaction> findAllByOrderByCreatedAtDesc();
}
