package com.hms.purchaseservice.repository;

import com.hms.purchaseservice.entity.QuotationStatus;
import com.hms.purchaseservice.entity.SupplierQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierQuotationRepository extends JpaRepository<SupplierQuotation, Long> {
    Optional<SupplierQuotation> findByQuoteCode(String quoteCode);
    List<SupplierQuotation> findByPurchaseRequestId(Long purchaseRequestId);
    List<SupplierQuotation> findBySupplierId(Long supplierId);
    List<SupplierQuotation> findByStatus(QuotationStatus status);
}
