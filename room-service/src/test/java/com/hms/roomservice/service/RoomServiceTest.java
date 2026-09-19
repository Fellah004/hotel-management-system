package com.hms.roomservice.service;

import com.hms.roomservice.dto.request.CreateRoomRequest;
import com.hms.roomservice.dto.request.RoomStatusUpdateRequest;
import com.hms.roomservice.dto.response.RoomResponse;
import com.hms.roomservice.entity.*;
import com.hms.roomservice.event.publisher.RoomEventPublisher;
import com.hms.roomservice.exception.BusinessRuleException;
import com.hms.roomservice.exception.DuplicateResourceException;
import com.hms.roomservice.exception.ForbiddenException;
import com.hms.roomservice.repository.AmenityRepository;
import com.hms.roomservice.repository.RoomCategoryRepository;
import com.hms.roomservice.repository.RoomRepository;
import com.hms.roomservice.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomCategoryRepository categoryRepository;

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private RoomEventPublisher roomEventPublisher;

    @InjectMocks
    private RoomServiceImpl roomService;

    private RoomCategory sampleCategory;
    private Room sampleRoom;

    @BeforeEach
    void setUp() {
        sampleCategory = RoomCategory.builder()
                .id(1L)
                .name("DELUXE")
                .description("Deluxe suite")
                .basePrice(new BigDecimal("250.00"))
                .maxOccupancy(3)
                .build();

        sampleRoom = Room.builder()
                .id(100L)
                .roomNumber("201")
                .category(sampleCategory)
                .resourceType(ResourceType.ROOM)
                .floor(2)
                .capacity(3)
                .pricePerNight(new BigDecimal("250.00"))
                .status(RoomStatus.AVAILABLE)
                .active(true)
                .amenities(new HashSet<>())
                .build();
    }

    @Test
    void createRoom_Success() {
        CreateRoomRequest request = CreateRoomRequest.builder()
                .roomNumber("201")
                .categoryId(1L)
                .floor(2)
                .capacity(3)
                .pricePerNight(new BigDecimal("250.00"))
                .build();

        when(roomRepository.existsByRoomNumber("201")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> {
            Room r = i.getArgument(0);
            r.setId(100L);
            return r;
        });

        RoomResponse response = roomService.createRoom(request);

        assertNotNull(response);
        assertEquals("201", response.getRoomNumber());
        assertEquals(RoomStatus.AVAILABLE, response.getStatus());
    }

    @Test
    void createRoom_DuplicateRoomNumber_ThrowsDuplicateResourceException() {
        CreateRoomRequest request = CreateRoomRequest.builder()
                .roomNumber("201")
                .categoryId(1L)
                .floor(2)
                .capacity(3)
                .pricePerNight(new BigDecimal("250.00"))
                .build();

        when(roomRepository.existsByRoomNumber("201")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> roomService.createRoom(request));
    }

    @Test
    void updateRoomStatus_ValidTransition_Success() {
        when(roomRepository.findById(100L)).thenReturn(Optional.of(sampleRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(sampleRoom);

        RoomStatusUpdateRequest request = new RoomStatusUpdateRequest(RoomStatus.MAINTENANCE, "AC repair");
        RoomResponse response = roomService.updateRoomStatus(100L, request, "MANAGER");

        assertNotNull(response);
        assertEquals(RoomStatus.MAINTENANCE, sampleRoom.getStatus());
        verify(roomEventPublisher).publishRoomStatusChanged(100L, "201", RoomStatus.AVAILABLE, RoomStatus.MAINTENANCE);
    }

    @Test
    void updateRoomStatus_HousekeeperCannotSetAvailableDirectly_ThrowsForbiddenException() {
        sampleRoom.setStatus(RoomStatus.CLEAN);
        when(roomRepository.findById(100L)).thenReturn(Optional.of(sampleRoom));

        RoomStatusUpdateRequest request = new RoomStatusUpdateRequest(RoomStatus.AVAILABLE, "Cleaned");

        assertThrows(ForbiddenException.class, () -> roomService.updateRoomStatus(100L, request, "HOUSEKEEPER"));
    }

    @Test
    void updateRoomStatus_InvalidTransition_ThrowsBusinessRuleException() {
        sampleRoom.setStatus(RoomStatus.DIRTY);
        when(roomRepository.findById(100L)).thenReturn(Optional.of(sampleRoom));

        // DIRTY cannot jump directly to AVAILABLE
        RoomStatusUpdateRequest request = new RoomStatusUpdateRequest(RoomStatus.AVAILABLE, "Skip clean");

        assertThrows(BusinessRuleException.class, () -> roomService.updateRoomStatus(100L, request, "MANAGER"));
    }
}
