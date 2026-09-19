package com.hms.staffservice.repository;

import com.hms.staffservice.entity.Shift;
import com.hms.staffservice.entity.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    Optional<Shift> findByName(ShiftType name);
    List<Shift> findByActiveTrue();

    @Query("SELECT s FROM Shift s JOIN s.assignedStaffIds staffId WHERE staffId = :staffId AND s.active = true")
    List<Shift> findShiftsByStaffId(@Param("staffId") Long staffId);
}
