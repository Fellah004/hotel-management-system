package com.hms.guestexperienceservice.controller;

import com.hms.guestexperienceservice.dto.request.EarnLoyaltyRequest;
import com.hms.guestexperienceservice.dto.request.RedeemLoyaltyRequest;
import com.hms.guestexperienceservice.dto.response.LoyaltyAccountResponse;
import com.hms.guestexperienceservice.dto.response.LoyaltyTransactionResponse;
import com.hms.guestexperienceservice.service.GuestExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty Program", description = "APIs for guest loyalty tiers (Silver, Gold, Platinum), points earning, and redemption")
@SecurityRequirement(name = "BearerAuth")
public class LoyaltyController {

    private final GuestExperienceService guestExperienceService;

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get loyalty account", description = "Retrieves loyalty account and current tier for a guest")
    public ResponseEntity<LoyaltyAccountResponse> getLoyaltyAccountByGuestId(@PathVariable Long guestId) {
        return ResponseEntity.ok(guestExperienceService.getLoyaltyAccountByGuestId(guestId));
    }

    @PostMapping("/earn")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Earn loyalty points", description = "Awards loyalty points to a guest account")
    public ResponseEntity<LoyaltyAccountResponse> earnPoints(@Valid @RequestBody EarnLoyaltyRequest request) {
        return ResponseEntity.ok(guestExperienceService.earnPoints(request));
    }

    @PostMapping("/redeem")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'GUEST')")
    @Operation(summary = "Redeem loyalty points", description = "Redeems points from a guest loyalty account")
    public ResponseEntity<LoyaltyAccountResponse> redeemPoints(@Valid @RequestBody RedeemLoyaltyRequest request) {
        return ResponseEntity.ok(guestExperienceService.redeemPoints(request));
    }

    @GetMapping("/{accountId}/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get loyalty transactions", description = "Retrieves immutable audit history of loyalty points transactions")
    public ResponseEntity<List<LoyaltyTransactionResponse>> getLoyaltyTransactions(@PathVariable Long accountId) {
        return ResponseEntity.ok(guestExperienceService.getLoyaltyTransactions(accountId));
    }
}
