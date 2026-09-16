package com.hms.roomservice.service.impl;

import com.hms.roomservice.dto.request.CreateRoomRequest;
import com.hms.roomservice.dto.request.RoomStatusUpdateRequest;
import com.hms.roomservice.dto.request.UpdateRoomRequest;
import com.hms.roomservice.dto.response.AmenityResponse;
import com.hms.roomservice.dto.response.RoomCategoryResponse;
import com.hms.roomservice.dto.response.RoomResponse;
import com.hms.roomservice.entity.*;
import com.hms.roomservice.event.publisher.RoomEventPublisher;
import com.hms.roomservice.exception.BusinessRuleException;
import com.hms.roomservice.exception.DuplicateResourceException;
import com.hms.roomservice.exception.ForbiddenException;
import com.hms.roomservice.exception.ResourceNotFoundException;
import com.hms.roomservice.repository.AmenityRepository;
import com.hms.roomservice.repository.RoomCategoryRepository;
import com.hms.roomservice.repository.RoomRepository;
import com.hms.roomservice.service.RoomService;
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
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomCategoryRepository categoryRepository;
    private final AmenityRepository amenityRepository;
    private final RoomEventPublisher roomEventPublisher;

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new DuplicateResourceException("Room already exists with number: " + request.getRoomNumber());
        }

        RoomCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Room category not found with id: " + request.getCategoryId()));

        Set<Amenity> amenities = new HashSet<>();
        if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {
            amenities = new HashSet<>(amenityRepository.findAllById(request.getAmenityIds()));
        }

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .category(category)
                .resourceType(request.getResourceType() != null ? request.getResourceType() : ResourceType.ROOM)
                .floor(request.getFloor())
                .capacity(request.getCapacity())
                .pricePerNight(request.getPricePerNight())
                .status(RoomStatus.AVAILABLE)
                .active(true)
                .amenities(amenities)
                .build();

        Room saved = roomRepository.save(room);
        log.info("Created room number: {} (id: {})", saved.getRoomNumber(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        return mapToResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomByNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with number: " + roomNumber));
        return mapToResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomServiceImpl::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms(Long categoryId, Integer minCapacity, ResourceType resourceType) {
        return roomRepository.findAvailableRooms(RoomStatus.AVAILABLE, categoryId, minCapacity, resourceType).stream()
                .map(RoomServiceImpl::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByStatus(RoomStatus status) {
        return roomRepository.findByStatus(status).stream()
                .map(RoomServiceImpl::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        if (request.getCategoryId() != null) {
            RoomCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room category not found with id: " + request.getCategoryId()));
            room.setCategory(category);
        }

        if (request.getResourceType() != null) room.setResourceType(request.getResourceType());
        if (request.getFloor() != null) room.setFloor(request.getFloor());
        if (request.getCapacity() != null) room.setCapacity(request.getCapacity());
        if (request.getPricePerNight() != null) room.setPricePerNight(request.getPricePerNight());
        if (request.getActive() != null) room.setActive(request.getActive());

        if (request.getAmenityIds() != null) {
            Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(request.getAmenityIds()));
            room.setAmenities(amenities);
        }

        Room updated = roomRepository.save(room);
        log.info("Updated room id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public RoomResponse updateRoomStatus(Long id, RoomStatusUpdateRequest request, String userRole) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        RoomStatus oldStatus = room.getStatus();
        RoomStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            return mapToResponse(room);
        }

        // Validate state machine transitions
        validateStateTransition(oldStatus, newStatus, userRole);

        room.setStatus(newStatus);
        Room updated = roomRepository.save(room);

        // Publish event to RabbitMQ
        roomEventPublisher.publishRoomStatusChanged(updated.getId(), updated.getRoomNumber(), oldStatus, newStatus);
        log.info("Transitioned room {} status from {} to {}", room.getRoomNumber(), oldStatus, newStatus);

        return mapToResponse(updated);
    }

    private void validateStateTransition(RoomStatus current, RoomStatus target, String userRole) {
        // Enforce strict business rule: HOUSEKEEPER cannot directly set CLEAN -> AVAILABLE
        if (target == RoomStatus.AVAILABLE && "HOUSEKEEPER".equalsIgnoreCase(userRole)) {
            throw new ForbiddenException("Housekeepers cannot directly mark rooms as AVAILABLE. Room verification required by Manager or Front Desk.");
        }

        boolean valid = switch (current) {
            case AVAILABLE -> target == RoomStatus.RESERVED || target == RoomStatus.OCCUPIED || target == RoomStatus.MAINTENANCE || target == RoomStatus.OUT_OF_SERVICE;
            case RESERVED -> target == RoomStatus.OCCUPIED || target == RoomStatus.AVAILABLE;
            case OCCUPIED -> target == RoomStatus.DIRTY || target == RoomStatus.MAINTENANCE;
            case DIRTY -> target == RoomStatus.CLEANING || target == RoomStatus.MAINTENANCE;
            case CLEANING -> target == RoomStatus.CLEAN || target == RoomStatus.DIRTY;
            case CLEAN -> target == RoomStatus.AVAILABLE || target == RoomStatus.DIRTY || target == RoomStatus.OCCUPIED;
            case MAINTENANCE -> target == RoomStatus.AVAILABLE || target == RoomStatus.DIRTY || target == RoomStatus.OUT_OF_SERVICE;
            case OUT_OF_SERVICE -> target == RoomStatus.AVAILABLE || target == RoomStatus.MAINTENANCE;
        };

        if (!valid) {
            throw new BusinessRuleException("Invalid room status transition from " + current + " to " + target);
        }
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
        log.info("Deleted room id: {}", id);
    }

    public static RoomResponse mapToResponse(Room room) {
        if (room == null) return null;

        Set<AmenityResponse> amenityResponses = room.getAmenities() != null
                ? room.getAmenities().stream().map(AmenityServiceImpl::mapToResponse).collect(Collectors.toSet())
                : Set.of();

        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .category(RoomCategoryServiceImpl.mapToResponse(room.getCategory()))
                .resourceType(room.getResourceType())
                .floor(room.getFloor())
                .capacity(room.getCapacity())
                .pricePerNight(room.getPricePerNight())
                .status(room.getStatus())
                .active(room.isActive())
                .amenities(amenityResponses)
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
