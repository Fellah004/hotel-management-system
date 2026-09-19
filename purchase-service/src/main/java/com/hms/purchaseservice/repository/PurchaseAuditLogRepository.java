package com.hms.purchaseservice.repository;

import com.hms.purchaseservice.entity.PurchaseAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseAuditLogRepository extends JpaRepository<PurchaseAuditLog, Long> {
    List<PurchaseAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
    List<PurchaseAuditLog> findAllByOrderByCreatedAtDesc();
}
