package com.hms.inventoryservice.controller;

import com.hms.inventoryservice.dto.request.CreateItemRequest;
import com.hms.inventoryservice.dto.request.ReorderRuleRequest;
import com.hms.inventoryservice.dto.request.StockMovementRequest;
import com.hms.inventoryservice.dto.request.UpdateItemRequest;
import com.hms.inventoryservice.dto.response.ItemResponse;
import com.hms.inventoryservice.dto.response.ReorderRuleResponse;
import com.hms.inventoryservice.dto.response.StockMovementResponse;
import com.hms.inventoryservice.entity.InventoryCategory;
import com.hms.inventoryservice.service.InventoryService;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "Endpoints for inventory items, stock movements, and automatic low-stock alerts")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Create inventory item", description = "Admin/Owner/Manager creates a new inventory item")
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody CreateItemRequest request) {
        ItemResponse response = inventoryService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all inventory items", description = "Retrieves inventory catalog with optional category filter")
    public ResponseEntity<List<ItemResponse>> getAllItems(@RequestParam(required = false) InventoryCategory category) {
        if (category != null) {
            return ResponseEntity.ok(inventoryService.getItemsByCategory(category));
        }
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock items", description = "Retrieves inventory items whose stock is at or below threshold")
    public ResponseEntity<List<ItemResponse>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieves inventory item details by ID")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getItemById(id));
    }

    @GetMapping("/code/{itemCode}")
    @Operation(summary = "Get item by Item Code", description = "Retrieves inventory item by unique code")
    public ResponseEntity<ItemResponse> getItemByCode(@PathVariable String itemCode) {
        return ResponseEntity.ok(inventoryService.getItemByCode(itemCode));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Update inventory item", description = "Updates item properties and thresholds")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable Long id, @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(inventoryService.updateItem(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Delete inventory item", description = "Deletes an item from catalog (Admin/Owner only)")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stock-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER')")
    @Operation(summary = "Stock In (Replenishment)", description = "Adds quantity to stock and creates immutable movement log")
    public ResponseEntity<StockMovementResponse> stockIn(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(inventoryService.stockIn(id, request));
    }

    @PostMapping("/{id}/stock-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER')")
    @Operation(summary = "Stock Out (Usage)", description = "Consumes stock quantity, checks threshold, and triggers low-stock event if needed")
    public ResponseEntity<StockMovementResponse> stockOut(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(inventoryService.stockOut(id, request));
    }

    @PostMapping("/{id}/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Adjust Stock", description = "Auditing stock adjustment to match physical count")
    public ResponseEntity<StockMovementResponse> adjustStock(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(id, request));
    }

    @GetMapping("/{id}/movements")
    @Operation(summary = "Get item stock movements", description = "Retrieves full historical stock movement ledger for an item")
    public ResponseEntity<List<StockMovementResponse>> getMovementsByItem(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getMovementsByItem(id));
    }

    @PostMapping("/reorder-rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Configure reorder rule", description = "Sets automated reorder rules and supplier info")
    public ResponseEntity<ReorderRuleResponse> setReorderRule(@Valid @RequestBody ReorderRuleRequest request) {
        return ResponseEntity.ok(inventoryService.setReorderRule(request));
    }

    @GetMapping("/{id}/reorder-rules")
    @Operation(summary = "Get reorder rule for item", description = "Retrieves reorder rule by item ID")
    public ResponseEntity<ReorderRuleResponse> getReorderRuleByItem(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getReorderRuleByItem(id));
    }
}
