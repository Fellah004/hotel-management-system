package com.hms.roomservice.controller;

import com.hms.roomservice.dto.request.CreateRoomCategoryRequest;
import com.hms.roomservice.dto.response.RoomCategoryResponse;
import com.hms.roomservice.service.RoomCategoryService;
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
@RequestMapping("/api/room-categories")
@RequiredArgsConstructor
@Tag(name = "Room Categories", description = "Endpoints for managing lodging room categories (SINGLE, DOUBLE, DELUXE, SUITE, etc.)")
public class RoomCategoryController {

    private final RoomCategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create room category", description = "Creates a new category with base pricing and occupancy")
    public ResponseEntity<RoomCategoryResponse> createCategory(@Valid @RequestBody CreateRoomCategoryRequest request) {
        RoomCategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all room categories", description = "Public/Authorized endpoint listing all categories")
    public ResponseEntity<List<RoomCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room category by ID", description = "Retrieves room category details by ID")
    public ResponseEntity<RoomCategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update room category", description = "Updates category details and base price")
    public ResponseEntity<RoomCategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CreateRoomCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete room category", description = "Deletes a category (Admin/Owner only)")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
