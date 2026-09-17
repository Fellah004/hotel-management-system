package com.hms.staffservice.service.impl;

import com.hms.staffservice.dto.request.LeaveApprovalRequest;
import com.hms.staffservice.dto.request.LeaveRequest;
import com.hms.staffservice.dto.response.LeaveResponse;
import com.hms.staffservice.entity.LeaveStatus;
import com.hms.staffservice.entity.StaffLeave;
import com.hms.staffservice.exception.BusinessRuleException;
import com.hms.staffservice.exception.ResourceNotFoundException;
import com.hms.staffservice.repository.StaffLeaveRepository;
import com.hms.staffservice.repository.StaffRepository;
import com.hms.staffservice.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final StaffLeaveRepository leaveRepository;
    private final StaffRepository staffRepository;

    @Override
    @Transactional
    public LeaveResponse requestLeave(LeaveRequest request) {
        if (!staffRepository.existsById(request.getStaffId())) {
            throw new ResourceNotFoundException("Staff not found with id: " + request.getStaffId());
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
        log.info("Submitted leave request for staff id: {}", request.getStaffId());
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
