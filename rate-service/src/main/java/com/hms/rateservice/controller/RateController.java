package com.hms.rateservice.controller;

import com.hms.rateservice.dto.request.CreateRateRequest;
import com.hms.rateservice.dto.request.QuoteRequest;
import com.hms.rateservice.dto.request.UpdateRateRequest;
import com.hms.rateservice.dto.response.RateQuoteResponse;
import com.hms.rateservice.dto.response.RateResponse;
import com.hms.rateservice.service.RateService;
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
@RequestMapping("/api/rates")
@RequiredArgsConstructor
@Tag(name = "Rates & Dynamic Pricing", description = "Endpoints for configuring base rates, multipliers, and generating price quotes")
public class RateController {

    private final RateService rateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create rate configuration", description = "Admin/Owner/Manager creates rate rules and multipliers")
    public ResponseEntity<RateResponse> createRate(@Valid @RequestBody CreateRateRequest request) {
        RateResponse response = rateService.createRate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all rates", description = "Retrieves all rate configurations")
    public ResponseEntity<List<RateResponse>> getAllRates() {
        return ResponseEntity.ok(rateService.getAllRates());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get rate by ID", description = "Retrieves rate configuration by ID")
    public ResponseEntity<RateResponse> getRateById(@PathVariable Long id) {
        return ResponseEntity.ok(rateService.getRateById(id));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get active rate by category ID", description = "Retrieves active rate configuration for a room category")
    public ResponseEntity<RateResponse> getRateByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(rateService.getRateByCategoryId(categoryId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update rate configuration", description = "Updates rate attributes and multipliers")
    public ResponseEntity<RateResponse> updateRate(@PathVariable Long id, @Valid @RequestBody UpdateRateRequest request) {
        return ResponseEntity.ok(rateService.updateRate(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete rate configuration", description = "Deletes rate configuration (Admin/Owner only)")
    public ResponseEntity<Void> deleteRate(@PathVariable Long id) {
        rateService.deleteRate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quote")
    @Operation(summary = "Generate dynamic pricing quote", description = "Calculates exact stay pricing taking into account weekend, seasonal, occupancy, and first-night pricing")
    public ResponseEntity<RateQuoteResponse> calculateQuote(@Valid @RequestBody QuoteRequest request) {
        RateQuoteResponse response = rateService.calculateQuote(request);
        return ResponseEntity.ok(response);
    }
}
