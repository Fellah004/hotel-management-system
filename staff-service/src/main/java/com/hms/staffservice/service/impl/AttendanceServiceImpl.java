package com.hms.staffservice.service.impl;

import com.hms.staffservice.dto.request.AttendanceRequest;
import com.hms.staffservice.dto.response.AttendanceResponse;
import com.hms.staffservice.entity.Attendance;
import com.hms.staffservice.entity.AttendanceStatus;
import com.hms.staffservice.exception.BusinessRuleException;
import com.hms.staffservice.exception.ResourceNotFoundException;
import com.hms.staffservice.repository.AttendanceRepository;
import com.hms.staffservice.repository.StaffRepository;
import com.hms.staffservice.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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
        if (!staffRepository.existsById(request.getStaffId())) {
            throw new ResourceNotFoundException("Staff not found with id: " + request.getStaffId());
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

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Recorded attendance for staff id {} on {}", request.getStaffId(), request.getDate());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse checkIn(Long staffId, Long shiftId) {
        if (!staffRepository.existsById(staffId)) {
            throw new ResourceNotFoundException("Staff not found with id: " + staffId);
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

        attendance.setCheckInTime(LocalTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT);
        if (shiftId != null) attendance.setShiftId(shiftId);

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Staff {} checked in at {}", staffId, saved.getCheckInTime());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(Long staffId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByStaffIdAndDate(staffId, today)
                .orElseThrow(() -> new BusinessRuleException("No check-in record found for staff id " + staffId + " today"));

        if (attendance.getCheckOutTime() != null) {
            throw new BusinessRuleException("Staff already checked out today at " + attendance.getCheckOutTime());
        }

        attendance.setCheckOutTime(LocalTime.now());
        Attendance saved = attendanceRepository.save(attendance);
        log.info("Staff {} checked out at {}", staffId, saved.getCheckOutTime());
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

    private AttendanceResponse mapToResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .staffId(a.getStaffId())
                .date(a.getDate())
                .checkInTime(a.getCheckInTime())
                .checkOutTime(a.getCheckOutTime())
                .status(a.getStatus())
                .shiftId(a.getShiftId())
                .remarks(a.getRemarks())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
