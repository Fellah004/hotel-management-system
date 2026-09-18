package com.hms.purchaseservice.repository;

import com.hms.purchaseservice.entity.Supplier;
import com.hms.purchaseservice.entity.SupplierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findBySupplierCode(String supplierCode);
    List<Supplier> findByStatus(SupplierStatus status);
    boolean existsBySupplierCode(String supplierCode);
    boolean existsByEmail(String email);
}
