package com.hms.staffservice.service.impl;

import com.hms.staffservice.dto.request.AttendanceRequest;
import com.hms.staffservice.dto.response.AttendanceResponse;
import com.hms.staffservice.entity.Attendance;
import com.hms.staffservice.entity.AttendanceStatus;
import com.hms.staffservice.entity.Staff;
import com.hms.staffservice.exception.BusinessRuleException;
import com.hms.staffservice.exception.ResourceNotFoundException;
import com.hms.staffservice.repository.AttendanceRepository;
import com.hms.staffservice.repository.StaffRepository;
import com.hms.staffservice.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StaffRepository staffRepository;

    @Override
    @Transactional
    public AttendanceResponse recordAttendance(AttendanceRequest request) {
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + request.getStaffId()));

        if (request.getCheckInTime() != null && request.getCheckOutTime() != null) {
            if (request.getCheckOutTime().isBefore(request.getCheckInTime())) {
                throw new BusinessRuleException("Check-out time cannot be before check-in time for the same date");
            }
        }

        Attendance attendance = attendanceRepository.findByStaffIdAndDate(request.getStaffId(), request.getDate())
                .orElse(Attendance.builder()
                        .staffId(request.getStaffId())
                        .date(request.getDate())
                        .build());

        if (request.getCheckInTime() != null) attendance.setCheckInTime(request.getCheckInTime());
        if (request.getCheckOutTime() != null) attendance.setCheckOutTime(request.getCheckOutTime());
        if (request.getStatus() != null) attendance.setStatus(request.getStatus());
        if (request.getShiftId() != null) attendance.setShiftId(request.getShiftId());
        if (request.getRemarks() != null) attendance.setRemarks(request.getRemarks());

        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            attendance.setWorkHours(calculateWorkHours(attendance.getDate(), attendance.getCheckInTime(), attendance.getCheckOutTime()));
        }

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Recorded attendance for staff id {} on {}", request.getStaffId(), request.getDate());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse checkIn(Long staffId, Long shiftId) {
        return checkIn(staffId, shiftId, null, null);
    }

    @Override
    @Transactional
    public AttendanceResponse checkIn(Long staffId, Long shiftId, Long currentUserId, String userRole) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        if (!staff.isActive()) {
            throw new BusinessRuleException("Cannot clock in: Staff member is inactive or terminated.");
        }

        // Ownership validation: Staff can only clock in for themselves unless MANAGER, ADMIN, OWNER
        if (currentUserId != null && staff.getUserId() != null
                && !staff.getUserId().equals(currentUserId)
                && !"ADMIN".equals(userRole) && !"OWNER".equals(userRole) && !"MANAGER".equals(userRole)) {
            throw new BusinessRuleException("Access denied: You are only permitted to clock in for your own account.");
        }

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByStaffIdAndDate(staffId, today)
                .orElse(Attendance.builder()
                        .staffId(staffId)
                        .date(today)
                        .status(AttendanceStatus.PRESENT)
                        .shiftId(shiftId)
                        .build());

        if (attendance.getCheckInTime() != null) {
            throw new BusinessRuleException("Staff already checked in today at " + attendance.getCheckInTime());
        }

        attendance.setCheckInTime(LocalTime.now().truncatedTo(ChronoUnit.SECONDS));
        attendance.setStatus(AttendanceStatus.PRESENT);
        if (shiftId != null) attendance.setShiftId(shiftId);

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Staff {} checked in at {}", staffId, saved.getCheckInTime());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(Long staffId) {
        return checkOut(staffId, null, null);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(Long staffId, Long currentUserId, String userRole) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        // Ownership validation
        if (currentUserId != null && staff.getUserId() != null
                && !staff.getUserId().equals(currentUserId)
                && !"ADMIN".equals(userRole) && !"OWNER".equals(userRole) && !"MANAGER".equals(userRole)) {
            throw new BusinessRuleException("Access denied: You are only permitted to clock out for your own account.");
        }

        LocalDate today = LocalDate.now();

        // 1. Try finding today's open check-in
        Attendance attendance = attendanceRepository.findByStaffIdAndDate(staffId, today)
                .filter(a -> a.getCheckInTime() != null && a.getCheckOutTime() == null)
                .orElse(null);

        // 2. If not found, check yesterday's open check-in (supporting cross-midnight / night shifts)
        if (attendance == null) {
            attendance = attendanceRepository.findByStaffIdAndDate(staffId, today.minusDays(1))
                    .filter(a -> a.getCheckInTime() != null && a.getCheckOutTime() == null)
                    .orElse(null);
        }

        if (attendance == null) {
            Attendance todayRecord = attendanceRepository.findByStaffIdAndDate(staffId, today).orElse(null);
            if (todayRecord != null && todayRecord.getCheckOutTime() != null) {
                throw new BusinessRuleException("Staff already checked out today at " + todayRecord.getCheckOutTime());
            }
            throw new BusinessRuleException("No open check-in record found for staff id " + staffId);
        }

        attendance.setCheckOutTime(LocalTime.now().truncatedTo(ChronoUnit.SECONDS));
        attendance.setWorkHours(calculateWorkHours(attendance.getDate(), attendance.getCheckInTime(), attendance.getCheckOutTime()));

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Staff {} checked out at {}. Total work hours: {}", staffId, saved.getCheckOutTime(), saved.getWorkHours());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByStaff(Long staffId) {
        return attendanceRepository.findByStaffId(staffId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Double calculateWorkHours(LocalDate date, LocalTime checkIn, LocalTime checkOut) {
        if (checkIn == null || checkOut == null) return null;
        LocalDateTime start = LocalDateTime.of(date, checkIn);
        LocalDateTime end = checkOut.isBefore(checkIn)
                ? LocalDateTime.of(date.plusDays(1), checkOut)
                : LocalDateTime.of(date, checkOut);
        long minutes = Duration.between(start, end).toMinutes();
        return BigDecimal.valueOf(minutes / 60.0).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private AttendanceResponse mapToResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .staffId(a.getStaffId())
                .date(a.getDate())
                .checkInTime(a.getCheckInTime())
                .checkOutTime(a.getCheckOutTime())
                .status(a.getStatus())
                .shiftId(a.getShiftId())
                .workHours(a.getWorkHours())
                .remarks(a.getRemarks())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
