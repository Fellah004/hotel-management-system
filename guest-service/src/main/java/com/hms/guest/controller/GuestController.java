package com.hms.guest.controller;

import com.hms.guest.entity.Guest;
import com.hms.guest.entity.GuestDocument;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    @PostMapping
    public ResponseEntity<Guest> createGuest(
            @Valid @RequestBody Guest guest) {

        return ResponseEntity.ok(guest);
    }

    @GetMapping
    public ResponseEntity<List<Guest>> getAllGuests() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getGuestById(
            @PathVariable UUID id) {

        return ResponseEntity.ok("Guest ID: " + id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Guest> updateGuest(
            @PathVariable UUID id,
            @Valid @RequestBody Guest guest) {

        return ResponseEntity.ok(guest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuest(
            @PathVariable UUID id) {

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/documents")
    public ResponseEntity<GuestDocument> addDocument(
            @Valid @RequestBody GuestDocument document) {

        return ResponseEntity.ok(document);
    }

    @GetMapping("/{guestId}/documents")
    public ResponseEntity<List<GuestDocument>> getDocuments(
            @PathVariable UUID guestId) {

        return ResponseEntity.ok(List.of());
    }
}
