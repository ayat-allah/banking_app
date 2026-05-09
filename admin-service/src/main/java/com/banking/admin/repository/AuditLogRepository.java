package com.banking.admin.repository;

import com.banking.admin.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findAllByOrderByTimestampDesc();
    List<AuditLog> findByAdminIdOrderByTimestampDesc(String adminId);
    List<AuditLog> findByTargetUserIdOrderByTimestampDesc(String targetUserId);
}
