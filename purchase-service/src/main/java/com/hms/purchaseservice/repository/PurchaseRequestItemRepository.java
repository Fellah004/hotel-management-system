package com.hms.purchaseservice.repository;

import com.hms.purchaseservice.entity.PurchaseRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRequestItemRepository extends JpaRepository<PurchaseRequestItem, Long> {
    List<PurchaseRequestItem> findByPurchaseRequestId(Long purchaseRequestId);
}
