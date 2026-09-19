package com.hms.staffservice.repository;

import com.hms.staffservice.entity.LeaveStatus;
import com.hms.staffservice.entity.StaffLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StaffLeaveRepository extends JpaRepository<StaffLeave, Long> {
    List<StaffLeave> findByStaffId(Long staffId);
    List<StaffLeave> findByStatus(LeaveStatus status);
    List<StaffLeave> findByStaffIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long staffId, LocalDate end, LocalDate start);
}
