package com.hms.roomservice.service.impl;

import com.hms.roomservice.dto.request.CreateHallCategoryRequest;
import com.hms.roomservice.dto.request.UpdateHallCategoryRequest;
import com.hms.roomservice.dto.response.HallCategoryResponse;
import com.hms.roomservice.entity.HallCategory;
import com.hms.roomservice.exception.DuplicateResourceException;
import com.hms.roomservice.exception.ResourceNotFoundException;
import com.hms.roomservice.repository.HallCategoryRepository;
import com.hms.roomservice.service.HallCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HallCategoryServiceImpl implements HallCategoryService {

    private final HallCategoryRepository hallCategoryRepository;

    @Override
    @Transactional
    public HallCategoryResponse createCategory(CreateHallCategoryRequest request) {
        if (hallCategoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Hall category already exists with name: " + request.getName());
        }

        HallCategory category = HallCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePricePerHour(request.getBasePricePerHour())
                .basePricePerDay(request.getBasePricePerDay())
                .minCapacity(request.getMinCapacity())
                .maxCapacity(request.getMaxCapacity())
                .build();

        HallCategory saved = hallCategoryRepository.save(category);
        log.info("Created hall category: {}", saved.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HallCategoryResponse getCategoryById(Long id) {
        HallCategory category = hallCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall category not found with id: " + id));
        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallCategoryResponse> getAllCategories() {
        return hallCategoryRepository.findAll().stream()
                .map(HallCategoryServiceImpl::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HallCategoryResponse updateCategory(Long id, UpdateHallCategoryRequest request) {
        HallCategory category = hallCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall category not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            if (!category.getName().equalsIgnoreCase(request.getName()) && hallCategoryRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Hall category already exists with name: " + request.getName());
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getBasePricePerHour() != null) {
            category.setBasePricePerHour(request.getBasePricePerHour());
        }
        if (request.getBasePricePerDay() != null) {
            category.setBasePricePerDay(request.getBasePricePerDay());
        }
        if (request.getMinCapacity() != null) {
            category.setMinCapacity(request.getMinCapacity());
        }
        if (request.getMaxCapacity() != null) {
            category.setMaxCapacity(request.getMaxCapacity());
        }

        HallCategory updated = hallCategoryRepository.save(category);
        log.info("Updated hall category id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!hallCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hall category not found with id: " + id);
        }
        hallCategoryRepository.deleteById(id);
        log.info("Deleted hall category id: {}", id);
    }

    public static HallCategoryResponse mapToResponse(HallCategory category) {
        if (category == null) return null;
        return HallCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .basePricePerHour(category.getBasePricePerHour())
                .basePricePerDay(category.getBasePricePerDay())
                .minCapacity(category.getMinCapacity())
                .maxCapacity(category.getMaxCapacity())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
