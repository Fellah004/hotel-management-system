package com.hms.staffservice.service;

import com.hms.staffservice.dto.request.LeaveApprovalRequest;
import com.hms.staffservice.dto.request.LeaveRequest;
import com.hms.staffservice.dto.response.LeaveResponse;

import java.util.List;

public interface LeaveService {
    LeaveResponse requestLeave(LeaveRequest request);
    LeaveResponse requestLeave(LeaveRequest request, Long currentUserId);
    LeaveResponse approveOrRejectLeave(Long leaveId, LeaveApprovalRequest request, Long approverStaffId);
    LeaveResponse getLeaveById(Long id);
    List<LeaveResponse> getLeavesByStaff(Long staffId);
    List<LeaveResponse> getAllLeaves();
}
