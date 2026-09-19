package com.hms.roomservice.service.impl;

import com.hms.roomservice.dto.request.CreateHallRequest;
import com.hms.roomservice.dto.request.HallStatusUpdateRequest;
import com.hms.roomservice.dto.request.UpdateHallRequest;
import com.hms.roomservice.dto.response.AmenityResponse;
import com.hms.roomservice.dto.response.HallResponse;
import com.hms.roomservice.entity.Amenity;
import com.hms.roomservice.entity.Hall;
import com.hms.roomservice.entity.HallCategory;
import com.hms.roomservice.entity.HallStatus;
import com.hms.roomservice.exception.DuplicateResourceException;
import com.hms.roomservice.exception.ResourceNotFoundException;
import com.hms.roomservice.repository.AmenityRepository;
import com.hms.roomservice.repository.HallCategoryRepository;
import com.hms.roomservice.repository.HallRepository;
import com.hms.roomservice.service.HallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final HallCategoryRepository hallCategoryRepository;
    private final AmenityRepository amenityRepository;

    @Override
    @Transactional
    public HallResponse createHall(CreateHallRequest request) {
        if (hallRepository.existsByHallNumber(request.getHallNumber())) {
            throw new DuplicateResourceException("Hall already exists with number: " + request.getHallNumber());
        }

        HallCategory category = hallCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall category not found with id: " + request.getCategoryId()));

        Set<Amenity> amenities = new HashSet<>();
        if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {
            amenities = new HashSet<>(amenityRepository.findAllById(request.getAmenityIds()));
        }

        Hall hall = Hall.builder()
                .hallNumber(request.getHallNumber())
                .name(request.getName())
                .category(category)
                .floor(request.getFloor())
                .totalAreaSqFt(request.getTotalAreaSqFt())
                .theaterCapacity(request.getTheaterCapacity())
                .uShapeCapacity(request.getUShapeCapacity())
                .clusterCapacity(request.getClusterCapacity())
                .classroomCapacity(request.getClassroomCapacity())
                .pricePerHour(request.getPricePerHour())
                .pricePerDay(request.getPricePerDay())
                .status(HallStatus.AVAILABLE)
                .active(true)
                .amenities(amenities)
                .build();

        Hall saved = hallRepository.save(hall);
        log.info("Created hall: {} (id: {})", saved.getHallNumber(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HallResponse getHallById(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));
        return mapToResponse(hall);
    }

    @Override
    @Transactional(readOnly = true)
    public HallResponse getHallByNumber(String hallNumber) {
        Hall hall = hallRepository.findByHallNumber(hallNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with number: " + hallNumber));
        return mapToResponse(hall);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallResponse> getAllHalls() {
        return getAllHalls(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallResponse> getAllHalls(boolean includeInactive) {
        List<Hall> halls = includeInactive ? hallRepository.findAll() : hallRepository.findByActiveTrue();
        return halls.stream()
                .map(HallServiceImpl::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallResponse> getAvailableHalls(Long categoryId, Integer minCapacity) {
        return hallRepository.findAvailableHalls(HallStatus.AVAILABLE, categoryId, minCapacity).stream()
                .map(HallServiceImpl::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallResponse> getHallsByStatus(HallStatus status) {
        return getHallsByStatus(status, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallResponse> getHallsByStatus(HallStatus status, boolean includeInactive) {
        List<Hall> halls = includeInactive ? hallRepository.findByStatus(status) : hallRepository.findByStatusAndActiveTrue(status);
        return halls.stream()
                .map(HallServiceImpl::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HallResponse updateHall(Long id, UpdateHallRequest request) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));

        if (request.getName() != null) {
            hall.setName(request.getName());
        }
        if (request.getCategoryId() != null) {
            HallCategory category = hallCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hall category not found with id: " + request.getCategoryId()));
            hall.setCategory(category);
        }
        if (request.getFloor() != null) {
            hall.setFloor(request.getFloor());
        }
        if (request.getTotalAreaSqFt() != null) {
            hall.setTotalAreaSqFt(request.getTotalAreaSqFt());
        }
        if (request.getTheaterCapacity() != null) {
            hall.setTheaterCapacity(request.getTheaterCapacity());
        }
        if (request.getUShapeCapacity() != null) {
            hall.setUShapeCapacity(request.getUShapeCapacity());
        }
        if (request.getClusterCapacity() != null) {
            hall.setClusterCapacity(request.getClusterCapacity());
        }
        if (request.getClassroomCapacity() != null) {
            hall.setClassroomCapacity(request.getClassroomCapacity());
        }
        if (request.getPricePerHour() != null) {
            hall.setPricePerHour(request.getPricePerHour());
        }
        if (request.getPricePerDay() != null) {
            hall.setPricePerDay(request.getPricePerDay());
        }
        if (request.getActive() != null) {
            hall.setActive(request.getActive());
        }
        if (request.getAmenityIds() != null) {
            Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(request.getAmenityIds()));
            hall.setAmenities(amenities);
        }

        Hall updated = hallRepository.save(hall);
        log.info("Updated hall id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public HallResponse activateHall(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));
        hall.setActive(true);
        Hall updated = hallRepository.save(hall);
        log.info("Activated hall {} (id: {})", updated.getHallNumber(), updated.getId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public HallResponse deactivateHall(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));
        hall.setActive(false);
        Hall updated = hallRepository.save(hall);
        log.info("Deactivated hall {} (id: {})", updated.getHallNumber(), updated.getId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public HallResponse updateHallStatus(Long id, HallStatusUpdateRequest request) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));

        if (!hall.isActive()) {
            throw new com.hms.roomservice.exception.BusinessRuleException("Cannot update status of a deactivated hall. Please activate the hall first.");
        }

        hall.setStatus(request.getStatus());
        Hall updated = hallRepository.save(hall);
        log.info("Updated hall {} status to {}", hall.getHallNumber(), request.getStatus());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteHall(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));

        hall.getAmenities().clear();
        hallRepository.delete(hall);
        log.info("Deleted hall id: {}", id);
    }

    public static HallResponse mapToResponse(Hall hall) {
        if (hall == null) return null;

        Set<AmenityResponse> amenityResponses = hall.getAmenities() != null ?
                hall.getAmenities().stream()
                        .map(AmenityServiceImpl::mapToResponse)
                        .collect(Collectors.toSet()) : new HashSet<>();

        return HallResponse.builder()
                .id(hall.getId())
                .hallNumber(hall.getHallNumber())
                .name(hall.getName())
                .category(HallCategoryServiceImpl.mapToResponse(hall.getCategory()))
                .floor(hall.getFloor())
                .totalAreaSqFt(hall.getTotalAreaSqFt())
                .theaterCapacity(hall.getTheaterCapacity())
                .uShapeCapacity(hall.getUShapeCapacity())
                .clusterCapacity(hall.getClusterCapacity())
                .classroomCapacity(hall.getClassroomCapacity())
                .pricePerHour(hall.getPricePerHour())
                .pricePerDay(hall.getPricePerDay())
                .status(hall.getStatus())
                .active(hall.isActive())
                .amenities(amenityResponses)
                .version(hall.getVersion())
                .createdAt(hall.getCreatedAt())
                .updatedAt(hall.getUpdatedAt())
                .build();
    }
}
