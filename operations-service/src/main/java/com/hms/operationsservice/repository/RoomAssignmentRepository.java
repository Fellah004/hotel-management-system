package com.hms.operationsservice.repository;

import com.hms.operationsservice.entity.RoomAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomAssignmentRepository extends JpaRepository<RoomAssignment, Long> {

    List<RoomAssignment> findByStaffId(Long staffId);

    @Query("SELECT ra FROM RoomAssignment ra WHERE ra.active = true " +
           "AND :roomNumber BETWEEN ra.startRoomNumber AND ra.endRoomNumber " +
           "AND ra.effectiveFrom <= :date " +
           "AND (ra.effectiveTo IS NULL OR ra.effectiveTo >= :date)")
    List<RoomAssignment> findActiveAssignmentsForRoom(@Param("roomNumber") Integer roomNumber, @Param("date") LocalDate date);
}
