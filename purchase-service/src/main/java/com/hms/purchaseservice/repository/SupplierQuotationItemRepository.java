package com.hms.purchaseservice.repository;

import com.hms.purchaseservice.entity.SupplierQuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierQuotationItemRepository extends JpaRepository<SupplierQuotationItem, Long> {
    List<SupplierQuotationItem> findByQuotationId(Long quotationId);
}
