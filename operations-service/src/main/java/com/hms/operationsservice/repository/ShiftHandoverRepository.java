package com.hms.operationsservice.repository;

import com.hms.operationsservice.entity.ShiftHandover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftHandoverRepository extends JpaRepository<ShiftHandover, Long> {
    List<ShiftHandover> findByOutgoingStaffIdOrIncomingStaffId(Long outgoingStaffId, Long incomingStaffId);
}
