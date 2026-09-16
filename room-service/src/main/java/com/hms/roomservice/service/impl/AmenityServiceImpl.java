package com.hms.roomservice.service.impl;

import com.hms.roomservice.dto.request.CreateAmenityRequest;
import com.hms.roomservice.dto.response.AmenityResponse;
import com.hms.roomservice.entity.Amenity;
import com.hms.roomservice.exception.DuplicateResourceException;
import com.hms.roomservice.exception.ResourceNotFoundException;
import com.hms.roomservice.repository.AmenityRepository;
import com.hms.roomservice.service.AmenityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;

    @Override
    @Transactional
    public AmenityResponse createAmenity(CreateAmenityRequest request) {
        if (amenityRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Amenity already exists with name: " + request.getName());
        }

        Amenity amenity = Amenity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .build();

        Amenity saved = amenityRepository.save(amenity);
        log.info("Created amenity: {}", saved.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AmenityResponse getAmenityById(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));
        return mapToResponse(amenity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(AmenityServiceImpl::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AmenityResponse updateAmenity(Long id, CreateAmenityRequest request) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));

        if (!amenity.getName().equalsIgnoreCase(request.getName()) && amenityRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Amenity already exists with name: " + request.getName());
        }

        amenity.setName(request.getName());
        amenity.setDescription(request.getDescription());
        amenity.setIcon(request.getIcon());

        Amenity updated = amenityRepository.save(amenity);
        log.info("Updated amenity id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAmenity(Long id) {
        if (!amenityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Amenity not found with id: " + id);
        }
        amenityRepository.deleteById(id);
        log.info("Deleted amenity id: {}", id);
    }

    public static AmenityResponse mapToResponse(Amenity amenity) {
        if (amenity == null) return null;
        return AmenityResponse.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .description(amenity.getDescription())
                .icon(amenity.getIcon())
                .createdAt(amenity.getCreatedAt())
                .updatedAt(amenity.getUpdatedAt())
                .build();
    }
}
