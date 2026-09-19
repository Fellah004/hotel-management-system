package com.hms.purchaseservice.controller;

import com.hms.purchaseservice.dto.response.PurchaseAuditLogResponse;
import com.hms.purchaseservice.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-audit")
@RequiredArgsConstructor
@Tag(name = "Purchase Audit & History", description = "Endpoints for viewing chronological audit trails and lifecycle changes")
public class PurchaseAuditController {

    private final PurchaseService purchaseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Get audit logs (all or filtered by entity type and ID)")
    public ResponseEntity<List<PurchaseAuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        if (entityType != null && entityId != null) {
            return ResponseEntity.ok(purchaseService.getAuditLogs(entityType, entityId));
        }
        return ResponseEntity.ok(purchaseService.getAllAuditLogs());
    }
}
