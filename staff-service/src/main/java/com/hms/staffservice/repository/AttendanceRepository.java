package com.hms.staffservice.repository;

import com.hms.staffservice.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStaffId(Long staffId);
    List<Attendance> findByDate(LocalDate date);
    Optional<Attendance> findByStaffIdAndDate(Long staffId, LocalDate date);
}
