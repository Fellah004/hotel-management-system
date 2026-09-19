package com.hms.guestexperienceservice.controller;

import com.hms.guestexperienceservice.dto.request.AssignComplaintRequest;
import com.hms.guestexperienceservice.dto.request.CreateComplaintRequest;
import com.hms.guestexperienceservice.dto.request.UpdateComplaintStatusRequest;
import com.hms.guestexperienceservice.dto.response.ComplaintHistoryResponse;
import com.hms.guestexperienceservice.dto.response.ComplaintResponse;
import com.hms.guestexperienceservice.service.GuestExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints Management", description = "APIs for registering, assigning, tracking, and resolving complaints")
@SecurityRequirement(name = "BearerAuth")
public class ComplaintController {

    private final GuestExperienceService guestExperienceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "File a complaint", description = "Registers a new guest complaint")
    public ResponseEntity<ComplaintResponse> createComplaint(@Valid @RequestBody CreateComplaintRequest request) {
        return new ResponseEntity<>(guestExperienceService.createComplaint(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "List all complaints", description = "Retrieves all complaints across the hotel")
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints() {
        return ResponseEntity.ok(guestExperienceService.getAllComplaints());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get complaint by ID", description = "Retrieves complaint details")
    public ResponseEntity<ComplaintResponse> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(guestExperienceService.getComplaintById(id));
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get complaints by guest ID", description = "Retrieves complaints filed by a guest")
    public ResponseEntity<List<ComplaintResponse>> getComplaintsByGuestId(@PathVariable Long guestId) {
        return ResponseEntity.ok(guestExperienceService.getComplaintsByGuestId(guestId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Update complaint status", description = "Updates complaint status and adds resolution remarks")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(@PathVariable Long id,
                                                                   @Valid @RequestBody UpdateComplaintStatusRequest request) {
        return ResponseEntity.ok(guestExperienceService.updateComplaintStatus(id, request));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Assign complaint to staff", description = "Assigns complaint to a designated staff member")
    public ResponseEntity<ComplaintResponse> assignComplaint(@PathVariable Long id,
                                                             @Valid @RequestBody AssignComplaintRequest request) {
        return ResponseEntity.ok(guestExperienceService.assignComplaint(id, request));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get complaint history", description = "Retrieves the status transition history of a complaint")
    public ResponseEntity<List<ComplaintHistoryResponse>> getComplaintHistory(@PathVariable Long id) {
        return ResponseEntity.ok(guestExperienceService.getComplaintHistory(id));
    }
}
