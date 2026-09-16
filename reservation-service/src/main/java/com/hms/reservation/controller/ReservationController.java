package com.hms.reservation.controller;

import com.hms.reservation.dto.AvailabilityResponse;
import com.hms.reservation.dto.ReservationRequest;
import com.hms.reservation.dto.ReservationResponse;
import com.hms.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','STAFF')")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<ReservationResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ReservationResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/code/{code}")
    public ReservationResponse getByCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    @GetMapping("/guest/{guestId}")
    public List<ReservationResponse> getByGuest(@PathVariable Long guestId) {
        return service.getByGuest(guestId);
    }

    @GetMapping("/room/{roomId}")
    public List<ReservationResponse> getByRoom(@PathVariable Long roomId) {
        return service.getByRoom(roomId);
    }

    @GetMapping("/availability")
    public List<AvailabilityResponse> searchAvailableRooms(
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut,
            @RequestParam Integer guests,
            @RequestParam(required = false) String roomType) {
        return service.searchAvailableRooms(
                checkIn, checkOut, guests, roomType
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','STAFF')")
    public ReservationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','STAFF')")
    public ReservationResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','STAFF')")
    public ReservationResponse checkIn(@PathVariable Long id) {
        return service.checkIn(id);
    }

    @PatchMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','STAFF')")
    public ReservationResponse checkOut(@PathVariable Long id) {
        return service.checkOut(id);
    }
}
