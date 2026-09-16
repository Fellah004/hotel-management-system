package com.hms.guestexperience.controller;

import com.hms.guestexperience.entity.GuestPreference;
import com.hms.guestexperience.entity.GuestRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guest-experience")
public class GuestExperienceController {

    @PostMapping("/preferences")
    public ResponseEntity<GuestPreference> savePreference(
            @Valid @RequestBody GuestPreference preference) {

        return ResponseEntity.ok(preference);
    }

    @GetMapping("/preferences/{guestId}")
    public ResponseEntity<List<GuestPreference>> getPreferences(
            @PathVariable UUID guestId) {

        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/requests")
    public ResponseEntity<GuestRequest> createRequest(
            @Valid @RequestBody GuestRequest request) {

        return ResponseEntity.ok(request);
    }

    @GetMapping("/requests/{guestId}")
    public ResponseEntity<List<GuestRequest>> getRequests(
            @PathVariable UUID guestId) {

        return ResponseEntity.ok(List.of());
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                "Guest request " + id + " status changed to " + status
        );
    }
}
