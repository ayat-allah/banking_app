package com.banking.transaction.repository;

import com.banking.transaction.model.MoneyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoneyRequestRepository extends JpaRepository<MoneyRequest, String> {
    List<MoneyRequest> findByRequesterIdOrderByCreatedAtDesc(String requesterId);
    List<MoneyRequest> findByRequesteeIdOrderByCreatedAtDesc(String requesteeId);
    List<MoneyRequest> findByRequesteeIdAndStatusOrderByCreatedAtDesc(
            String requesteeId, MoneyRequest.RequestStatus status);
}
