package com.hms.staffservice.service;

import com.hms.staffservice.dto.request.AttendanceRequest;
import com.hms.staffservice.dto.response.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceResponse recordAttendance(AttendanceRequest request);
    AttendanceResponse checkIn(Long staffId, Long shiftId);
    AttendanceResponse checkOut(Long staffId);
    List<AttendanceResponse> getAttendanceByStaff(Long staffId);
    List<AttendanceResponse> getAttendanceByDate(LocalDate date);
}
