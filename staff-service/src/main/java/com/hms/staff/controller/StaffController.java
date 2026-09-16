package com.hms.staff.controller;

import com.hms.staff.entity.Attendance;
import com.hms.staff.entity.Staff;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @PostMapping
    public ResponseEntity<Staff> createStaff(
            @Valid @RequestBody Staff staff) {

        return ResponseEntity.ok(staff);
    }

    @GetMapping
    public ResponseEntity<List<Staff>> getAllStaff() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getStaffById(
            @PathVariable UUID id) {

        return ResponseEntity.ok("Staff ID: " + id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Staff> updateStaff(
            @PathVariable UUID id,
            @Valid @RequestBody Staff staff) {

        return ResponseEntity.ok(staff);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(
            @PathVariable UUID id) {

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/attendance")
    public ResponseEntity<Attendance> recordAttendance(
            @Valid @RequestBody Attendance attendance) {

        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<Attendance>> getAttendance() {
        return ResponseEntity.ok(List.of());
    }
}
