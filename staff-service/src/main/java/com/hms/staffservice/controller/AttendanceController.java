package com.hms.staffservice.controller;

import com.hms.staffservice.dto.request.AttendanceRequest;
import com.hms.staffservice.dto.response.AttendanceResponse;
import com.hms.staffservice.service.AttendanceService;
import com.hms.staffservice.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "Endpoints for clocking in/out and tracking staff attendance")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Record staff attendance", description = "Manager records or adjusts attendance entry")
    public ResponseEntity<AttendanceResponse> recordAttendance(@Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.recordAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/check-in/{staffId}")
    @Operation(summary = "Clock-in", description = "Staff member clocks in for their shift")
    public ResponseEntity<AttendanceResponse> checkIn(
            @PathVariable Long staffId,
            @RequestParam(required = false) Long shiftId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long currentUserId = principal != null ? principal.getId() : null;
        String userRole = principal != null ? principal.getRole() : null;
        return ResponseEntity.ok(attendanceService.checkIn(staffId, shiftId, currentUserId, userRole));
    }

    @PostMapping("/check-out/{staffId}")
    @Operation(summary = "Clock-out", description = "Staff member clocks out at end of shift")
    public ResponseEntity<AttendanceResponse> checkOut(
            @PathVariable Long staffId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long currentUserId = principal != null ? principal.getId() : null;
        String userRole = principal != null ? principal.getRole() : null;
        return ResponseEntity.ok(attendanceService.checkOut(staffId, currentUserId, userRole));
    }

    @GetMapping("/staff/{staffId}")
    @Operation(summary = "Get staff attendance history", description = "Retrieves attendance records for a specific staff member")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByStaff(@PathVariable Long staffId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByStaff(staffId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Get daily attendance", description = "Retrieves attendance entries for a specific date")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(queryDate));
    }
}
