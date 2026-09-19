package com.hms.reservationservice.service;

import com.hms.reservationservice.client.GuestClient;
import com.hms.reservationservice.client.RateClient;
import com.hms.reservationservice.client.RoomClient;
import com.hms.reservationservice.client.dto.GuestDto;
import com.hms.reservationservice.client.dto.RateQuoteResponseDto;
import com.hms.reservationservice.dto.request.CreateReservationRequest;
import com.hms.reservationservice.dto.response.ReservationResponse;
import com.hms.reservationservice.entity.Reservation;
import com.hms.reservationservice.entity.ReservationStatus;
import com.hms.reservationservice.event.publisher.ReservationEventPublisher;
import com.hms.reservationservice.exception.BusinessRuleException;
import com.hms.reservationservice.repository.ReservationRepository;
import com.hms.reservationservice.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private GuestClient guestClient;

    @Mock
    private RoomClient roomClient;

    @Mock
    private RateClient rateClient;

    @Mock
    private ReservationEventPublisher eventPublisher;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Reservation sampleReservation;

    @BeforeEach
    void setUp() {
        sampleReservation = Reservation.builder()
                .id(1L)
                .reservationCode("RES-12345678")
                .guestId(10L)
                .resourceType("ROOM")
                .roomId(101L)
                .roomCategoryId(2L)
                .adults(2)
                .children(0)
                .checkInDateTime(LocalDateTime.now().plusDays(2))
                .checkOutDateTime(LocalDateTime.now().plusDays(5))
                .nights(3)
                .quotedAmount(new BigDecimal("450.00"))
                .status(ReservationStatus.CONFIRMED)
                .build();
    }

    @Test
    void createReservation_Success() {
        LocalDateTime checkIn = LocalDateTime.now().plusDays(2);
        LocalDateTime checkOut = LocalDateTime.now().plusDays(5);

        CreateReservationRequest request = CreateReservationRequest.builder()
                .guestId(10L)
                .roomCategoryId(2L)
                .roomId(101L)
                .adults(2)
                .checkInDateTime(checkIn)
                .checkOutDateTime(checkOut)
                .build();

        when(guestClient.getGuestById(10L)).thenReturn(GuestDto.builder().id(10L).userId(100L).build());
        when(reservationRepository.findOverlappingRoomReservations(eq(101L), any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(rateClient.calculateQuote(any())).thenReturn(RateQuoteResponseDto.builder()
                .totalQuotedAmount(new BigDecimal("480.00"))
                .build());

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReservationResponse response = reservationService.createReservation(request);

        assertNotNull(response);
        assertEquals(ReservationStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("480.00"), response.getQuotedAmount());
        verify(eventPublisher).publishReservationCreated(any(Reservation.class));
    }

    @Test
    void createReservation_DoubleBooking_ThrowsBusinessRuleException() {
        LocalDateTime checkIn = LocalDateTime.now().plusDays(2);
        LocalDateTime checkOut = LocalDateTime.now().plusDays(5);

        CreateReservationRequest request = CreateReservationRequest.builder()
                .guestId(10L)
                .roomCategoryId(2L)
                .roomId(101L)
                .adults(2)
                .checkInDateTime(checkIn)
                .checkOutDateTime(checkOut)
                .build();

        when(guestClient.getGuestById(10L)).thenReturn(GuestDto.builder().id(10L).userId(100L).build());
        // Mock that an overlapping reservation exists!
        when(reservationRepository.findOverlappingRoomReservations(eq(101L), any(), any(), any(), any()))
                .thenReturn(List.of(sampleReservation));

        assertThrows(BusinessRuleException.class, () -> reservationService.createReservation(request));
    }

    @Test
    void checkIn_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));
        when(reservationRepository.save(any())).thenReturn(sampleReservation);

        ReservationResponse response = reservationService.checkIn(1L);

        assertNotNull(response);
        assertEquals(ReservationStatus.CHECKED_IN, sampleReservation.getStatus());
        verify(eventPublisher).publishReservationCheckedIn(sampleReservation);
    }

    @Test
    void checkIn_PendingReservation_ThrowsBusinessRuleException() {
        sampleReservation.setStatus(ReservationStatus.PENDING);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));

        assertThrows(BusinessRuleException.class, () -> reservationService.checkIn(1L));
    }
}
