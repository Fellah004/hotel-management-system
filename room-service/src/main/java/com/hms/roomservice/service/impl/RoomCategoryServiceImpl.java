package com.hms.roomservice.service.impl;

import com.hms.roomservice.dto.request.CreateRoomCategoryRequest;
import com.hms.roomservice.dto.response.RoomCategoryResponse;
import com.hms.roomservice.entity.RoomCategory;
import com.hms.roomservice.exception.DuplicateResourceException;
import com.hms.roomservice.exception.ResourceNotFoundException;
import com.hms.roomservice.repository.RoomCategoryRepository;
import com.hms.roomservice.service.RoomCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomCategoryServiceImpl implements RoomCategoryService {

    private final RoomCategoryRepository categoryRepository;

    @Override
    @Transactional
    public RoomCategoryResponse createCategory(CreateRoomCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Room category already exists with name: " + request.getName());
        }

        RoomCategory category = RoomCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .maxOccupancy(request.getMaxOccupancy())
                .build();

        RoomCategory saved = categoryRepository.save(category);
        log.info("Created room category: {}", saved.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomCategoryResponse getCategoryById(Long id) {
        RoomCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room category not found with id: " + id));
        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(RoomCategoryServiceImpl::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoomCategoryResponse updateCategory(Long id, CreateRoomCategoryRequest request) {
        RoomCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room category not found with id: " + id));

        if (!category.getName().equalsIgnoreCase(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Room category already exists with name: " + request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setBasePrice(request.getBasePrice());
        category.setMaxOccupancy(request.getMaxOccupancy());

        RoomCategory updated = categoryRepository.save(category);
        log.info("Updated room category id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
        log.info("Deleted room category id: {}", id);
    }

    public static RoomCategoryResponse mapToResponse(RoomCategory category) {
        if (category == null) return null;
        return RoomCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .basePrice(category.getBasePrice())
                .maxOccupancy(category.getMaxOccupancy())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
