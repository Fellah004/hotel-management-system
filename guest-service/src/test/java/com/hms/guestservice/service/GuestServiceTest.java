package com.hms.guestservice.service;

import com.hms.guestservice.dto.request.CreateGuestRequest;
import com.hms.guestservice.dto.request.UpdateGuestRequest;
import com.hms.guestservice.dto.response.GuestResponse;
import com.hms.guestservice.entity.Guest;
import com.hms.guestservice.exception.DuplicateResourceException;
import com.hms.guestservice.exception.ForbiddenException;
import com.hms.guestservice.exception.ResourceNotFoundException;
import com.hms.guestservice.repository.GuestRepository;
import com.hms.guestservice.service.impl.GuestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private GuestServiceImpl guestService;

    private Guest sampleGuest;

    @BeforeEach
    void setUp() {
        sampleGuest = Guest.builder()
                .id(1L)
                .userId(10L)
                .memberCode("MEM-12345678")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .company("Acme Corp")
                .gender("MALE")
                .address("123 Main St")
                .build();
    }

    @Test
    void createGuest_Success() {
        CreateGuestRequest request = CreateGuestRequest.builder()
                .userId(10L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .company("Acme Corp")
                .gender("MALE")
                .address("123 Main St")
                .build();

        when(guestRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(guestRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(guestRepository.save(any(Guest.class))).thenAnswer(i -> {
            Guest g = i.getArgument(0);
            g.setId(1L);
            return g;
        });

        GuestResponse response = guestService.createGuest(request);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("john.doe@example.com", response.getEmail());
        assertTrue(response.getMemberCode().startsWith("MEM-"));
    }

    @Test
    void createGuest_DuplicateEmail_ThrowsDuplicateResourceException() {
        CreateGuestRequest request = CreateGuestRequest.builder()
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        when(guestRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> guestService.createGuest(request));
    }

    @Test
    void getGuestById_Staff_Success() {
        when(guestRepository.findById(1L)).thenReturn(Optional.of(sampleGuest));

        GuestResponse response = guestService.getGuestById(1L, 999L, "MANAGER");

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getGuestById_GuestOwnProfile_Success() {
        when(guestRepository.findById(1L)).thenReturn(Optional.of(sampleGuest));

        GuestResponse response = guestService.getGuestById(1L, 10L, "GUEST");

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getGuestById_GuestOtherProfile_ThrowsForbiddenException() {
        when(guestRepository.findById(1L)).thenReturn(Optional.of(sampleGuest));

        assertThrows(ForbiddenException.class, () -> guestService.getGuestById(1L, 20L, "GUEST"));
    }
}
