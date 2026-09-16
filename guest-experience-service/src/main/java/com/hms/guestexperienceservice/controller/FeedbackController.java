package com.hms.guestexperienceservice.controller;

import com.hms.guestexperienceservice.dto.request.CreateFeedbackRequest;
import com.hms.guestexperienceservice.dto.response.FeedbackResponse;
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
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Tag(name = "Guest Feedback", description = "APIs for guest ratings and reviews")
@SecurityRequirement(name = "BearerAuth")
public class FeedbackController {

    private final GuestExperienceService guestExperienceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'GUEST')")
    @Operation(summary = "Submit feedback", description = "Allows guests to submit ratings and feedback")
    public ResponseEntity<FeedbackResponse> submitFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        return new ResponseEntity<>(guestExperienceService.submitFeedback(request), HttpStatus.CREATED);
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'GUEST')")
    @Operation(summary = "Get feedback by guest", description = "Retrieves feedback submitted by a specific guest")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByGuestId(@PathVariable Long guestId) {
        return ResponseEntity.ok(guestExperienceService.getFeedbackByGuestId(guestId));
    }

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Get feedback by reservation", description = "Retrieves feedback for a specific reservation")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByReservationId(@PathVariable Long reservationId) {
        return ResponseEntity.ok(guestExperienceService.getFeedbackByReservationId(reservationId));
    }
}
