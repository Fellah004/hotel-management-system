package com.hms.roomservice.controller;

import com.hms.roomservice.dto.request.CreateHallCategoryRequest;
import com.hms.roomservice.dto.request.UpdateHallCategoryRequest;
import com.hms.roomservice.dto.response.HallCategoryResponse;
import com.hms.roomservice.service.HallCategoryService;
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
@RequestMapping("/api/hall-categories")
@RequiredArgsConstructor
@Tag(name = "Hall Categories", description = "Endpoints for managing event & banquet hall categories (BANQUET_HALL, CONFERENCE_HALL, BOARD_ROOM, etc.)")
public class HallCategoryController {

    private final HallCategoryService hallCategoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create hall category", description = "Admin/Owner/Manager creates a new hall category with hourly and daily pricing")
    public ResponseEntity<HallCategoryResponse> createCategory(@Valid @RequestBody CreateHallCategoryRequest request) {
        HallCategoryResponse response = hallCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all hall categories", description = "Public/Authorized endpoint listing all hall categories")
    public ResponseEntity<List<HallCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(hallCategoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hall category by ID", description = "Retrieves hall category details by ID")
    public ResponseEntity<HallCategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(hallCategoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update hall category", description = "Updates hall category details and pricing")
    public ResponseEntity<HallCategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateHallCategoryRequest request) {
        return ResponseEntity.ok(hallCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete hall category", description = "Deletes a hall category (Admin/Owner only)")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        hallCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
