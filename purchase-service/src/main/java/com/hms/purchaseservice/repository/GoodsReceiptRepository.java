package com.hms.purchaseservice.repository;

import com.hms.purchaseservice.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    Optional<GoodsReceipt> findByReceiptCode(String receiptCode);
    List<GoodsReceipt> findByPurchaseOrderId(Long purchaseOrderId);
}
