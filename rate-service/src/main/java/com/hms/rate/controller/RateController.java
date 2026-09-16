package com.hms.rate.controller;

import com.hms.rate.dto.RateRequest;
import com.hms.rate.dto.RateResponse;
import com.hms.rate.entity.RatePlan;
import com.hms.rate.entity.RoomType;
import com.hms.rate.service.RateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rates")
public class RateController {

    private final RateService service;

    public RateController(RateService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<RateResponse> create(
            @Valid @RequestBody RateRequest request,
            UriComponentsBuilder uriBuilder) {

        RateResponse response = service.create(request);
        return ResponseEntity
                .created(uriBuilder.path("/api/rates/{id}")
                        .buildAndExpand(response.getId()).toUri())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<RateResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RateResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RateResponse>> search(
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(required = false) RatePlan ratePlan,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(service.search(roomType, ratePlan, active));
    }

    @GetMapping("/effective")
    public ResponseEntity<List<RateResponse>> effective(
            @RequestParam RoomType roomType,
            @RequestParam(required = false) RatePlan ratePlan,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(service.getEffectiveRates(roomType, ratePlan, date));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<RateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<RateResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(service.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<RateResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
