package com.hms.purchaseservice.repository;

import com.hms.purchaseservice.entity.PurchaseRequest;
import com.hms.purchaseservice.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    Optional<PurchaseRequest> findByRequestCode(String requestCode);
    List<PurchaseRequest> findByStatus(RequestStatus status);
    List<PurchaseRequest> findByRequesterId(Long requesterId);
    List<PurchaseRequest> findByDepartmentId(Long departmentId);
}
