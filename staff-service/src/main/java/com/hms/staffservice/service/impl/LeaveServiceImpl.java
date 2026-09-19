package com.hms.staffservice.service.impl;

import com.hms.staffservice.dto.request.LeaveApprovalRequest;
import com.hms.staffservice.dto.request.LeaveRequest;
import com.hms.staffservice.dto.response.LeaveResponse;
import com.hms.staffservice.entity.Attendance;
import com.hms.staffservice.entity.AttendanceStatus;
import com.hms.staffservice.entity.LeaveStatus;
import com.hms.staffservice.entity.Staff;
import com.hms.staffservice.entity.StaffLeave;
import com.hms.staffservice.entity.StaffRole;
import com.hms.staffservice.exception.BusinessRuleException;
import com.hms.staffservice.exception.ResourceNotFoundException;
import com.hms.staffservice.repository.AttendanceRepository;
import com.hms.staffservice.repository.StaffLeaveRepository;
import com.hms.staffservice.repository.StaffRepository;
import com.hms.staffservice.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final StaffLeaveRepository leaveRepository;
    private final StaffRepository staffRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    @Transactional
    public LeaveResponse requestLeave(LeaveRequest request) {
        return requestLeave(request, null);
    }

    @Override
    @Transactional
    public LeaveResponse requestLeave(LeaveRequest request, Long currentUserId) {
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + request.getStaffId()));

        // Only RECEPTIONIST, HOUSEKEEPER, and MANAGER are permitted to apply for leave
        if (staff.getRole() == StaffRole.ADMIN || staff.getRole() == StaffRole.OWNER) {
            throw new BusinessRuleException("Only Receptionist, Housekeeper, and Manager are permitted to apply for leave.");
        }

        // Ownership validation: Staff can only submit leave applications for their own account
        if (currentUserId != null && staff.getUserId() != null && !staff.getUserId().equals(currentUserId)) {
            throw new BusinessRuleException("Access denied: You are only permitted to submit leave requests for yourself.");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessRuleException("Leave end date cannot be before start date");
        }

        StaffLeave leave = StaffLeave.builder()
                .staffId(request.getStaffId())
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        StaffLeave saved = leaveRepository.save(leave);
        log.info("Submitted leave request for staff id: {} (Role: {})", request.getStaffId(), staff.getRole());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public LeaveResponse approveOrRejectLeave(Long leaveId, LeaveApprovalRequest request, Long approverStaffId) {
        StaffLeave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveId));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessRuleException("Leave request is already " + leave.getStatus());
        }

        leave.setStatus(request.getStatus());
        leave.setApprovedByStaffId(approverStaffId);
        leave.setRemarks(request.getRemarks());

        StaffLeave updated = leaveRepository.save(leave);

        // Automatically sync attendance roster if leave is APPROVED
        if (request.getStatus() == LeaveStatus.APPROVED) {
            LocalDate current = leave.getStartDate();
            while (!current.isAfter(leave.getEndDate())) {
                LocalDate date = current;
                Attendance attendance = attendanceRepository.findByStaffIdAndDate(leave.getStaffId(), date)
                        .orElse(Attendance.builder()
                                .staffId(leave.getStaffId())
                                .date(date)
                                .build());
                attendance.setStatus(AttendanceStatus.ON_LEAVE);
                attendance.setRemarks("Approved Leave: " + leave.getLeaveType());
                attendanceRepository.save(attendance);
                current = current.plusDays(1);
            }
            log.info("Automatically marked attendance as ON_LEAVE for staff {} from {} to {}",
                    leave.getStaffId(), leave.getStartDate(), leave.getEndDate());
        }

        log.info("Leave request {} updated to status {}", leaveId, request.getStatus());
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long id) {
        StaffLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        return mapToResponse(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeavesByStaff(Long staffId) {
        return leaveRepository.findByStaffId(staffId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LeaveResponse mapToResponse(StaffLeave l) {
        return LeaveResponse.builder()
                .id(l.getId())
                .staffId(l.getStaffId())
                .leaveType(l.getLeaveType())
                .startDate(l.getStartDate())
                .endDate(l.getEndDate())
                .reason(l.getReason())
                .status(l.getStatus())
                .approvedByStaffId(l.getApprovedByStaffId())
                .remarks(l.getRemarks())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
