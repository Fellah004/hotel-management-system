package com.hms.staffservice.service;

import com.hms.staffservice.dto.request.ShiftRequest;
import com.hms.staffservice.dto.response.ShiftResponse;

import java.util.List;

public interface ShiftService {
    ShiftResponse createShift(ShiftRequest request);
    ShiftResponse getShiftById(Long id);
    List<ShiftResponse> getAllShifts();
    List<ShiftResponse> getShiftsByStaffId(Long staffId);
    ShiftResponse updateShift(Long id, ShiftRequest request);
    ShiftResponse assignStaffToShift(Long shiftId, Long staffId);
    ShiftResponse removeStaffFromShift(Long shiftId, Long staffId);
    void deleteShift(Long id);
}
