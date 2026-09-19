package com.hms.guestservice.controller;

import com.hms.guestservice.dto.request.CreateGuestRequest;
import com.hms.guestservice.dto.request.UpdateGuestRequest;
import com.hms.guestservice.dto.response.GuestResponse;
import com.hms.guestservice.security.UserPrincipal;
import com.hms.guestservice.service.GuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
@Tag(name = "Guest Management", description = "Endpoints for managing guest and member profiles")
@SecurityRequirement(name = "bearerAuth")
public class GuestController {

    private final GuestService guestService;

    @PostMapping
    @Operation(summary = "Create guest profile", description = "Creates a new guest profile with generated member code")
    public ResponseEntity<GuestResponse> createGuest(@Valid @RequestBody CreateGuestRequest request) {
        GuestResponse response = guestService.createGuest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Get all guests", description = "Retrieves all guest profiles (Staff access only)")
    public ResponseEntity<List<GuestResponse>> getAllGuests() {
        return ResponseEntity.ok(guestService.getAllGuests());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get guest by ID", description = "Retrieves guest profile with object-level ownership enforcement")
    public ResponseEntity<GuestResponse> getGuestById(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : null;
        return ResponseEntity.ok(guestService.getGuestById(id, userId, role));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get guest by User ID", description = "Retrieves guest profile by authentication user ID")
    public ResponseEntity<GuestResponse> getGuestByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(guestService.getGuestByUserId(userId));
    }

    @GetMapping("/member/{memberCode}")
    @Operation(summary = "Get guest by Member Code", description = "Retrieves guest profile by unique member code")
    public ResponseEntity<GuestResponse> getGuestByMemberCode(@PathVariable String memberCode) {
        return ResponseEntity.ok(guestService.getGuestByMemberCode(memberCode));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update guest profile", description = "Updates guest details with object-level check")
    public ResponseEntity<GuestResponse> updateGuest(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateGuestRequest request,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : null;
        return ResponseEntity.ok(guestService.updateGuest(id, request, userId, role));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Delete guest profile", description = "Deletes guest profile by ID (Admin/Owner only)")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.noContent().build();
    }
}
