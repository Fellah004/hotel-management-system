package com.hms.purchaseservice.repository;

import com.hms.purchaseservice.entity.OrderStatus;
import com.hms.purchaseservice.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByOrderCode(String orderCode);
    List<PurchaseOrder> findByStatus(OrderStatus status);
    List<PurchaseOrder> findBySupplierId(Long supplierId);
    List<PurchaseOrder> findByPurchaseRequestId(Long purchaseRequestId);
}
