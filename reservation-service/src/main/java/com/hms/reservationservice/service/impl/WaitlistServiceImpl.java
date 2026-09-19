package com.hms.reservationservice.service.impl;

import com.hms.reservationservice.client.RoomClient;
import com.hms.reservationservice.client.dto.RoomDto;
import com.hms.reservationservice.dto.request.CreateReservationRequest;
import com.hms.reservationservice.dto.request.WaitlistRequest;
import com.hms.reservationservice.dto.response.ReservationResponse;
import com.hms.reservationservice.dto.response.WaitlistResponse;
import com.hms.reservationservice.entity.Reservation;
import com.hms.reservationservice.entity.ReservationStatus;
import com.hms.reservationservice.entity.Waitlist;
import com.hms.reservationservice.entity.WaitlistStatus;
import com.hms.reservationservice.exception.BusinessRuleException;
import com.hms.reservationservice.exception.ResourceNotFoundException;
import com.hms.reservationservice.repository.ReservationRepository;
import com.hms.reservationservice.repository.WaitlistRepository;
import com.hms.reservationservice.service.ReservationService;
import com.hms.reservationservice.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistServiceImpl implements WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final RoomClient roomClient;

    private static final Set<ReservationStatus> ACTIVE_STATUSES = Set.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED,
            ReservationStatus.CHECKED_IN
    );

    @Override
    @Transactional
    public WaitlistResponse joinWaitlist(WaitlistRequest request) {
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BusinessRuleException("Check-out date must be after check-in date");
        }

        Waitlist waitlist = Waitlist.builder()
                .guestId(request.getGuestId())
                .roomCategoryId(request.getRoomCategoryId())
                .resourceType(request.getResourceType() != null ? request.getResourceType() : "ROOM")
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .guestsCount(request.getGuestsCount() != null ? request.getGuestsCount() : 1)
                .priority(1)
                .status(WaitlistStatus.ACTIVE)
                .build();

        Waitlist saved = waitlistRepository.save(waitlist);
        log.info("Guest {} joined waitlist for category {}", request.getGuestId(), request.getRoomCategoryId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WaitlistResponse getWaitlistById(Long id) {
        Waitlist waitlist = waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found with id: " + id));
        return mapToResponse(waitlist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitlistResponse> getWaitlistsByGuest(Long guestId) {
        return waitlistRepository.findByGuestId(guestId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitlistResponse> getAllActiveWaitlists() {
        return waitlistRepository.findByStatusOrderByPriorityDescCreatedAtAsc(WaitlistStatus.ACTIVE).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelWaitlist(Long id) {
        Waitlist waitlist = waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found with id: " + id));

        if (waitlist.getStatus() == WaitlistStatus.CONVERTED || waitlist.getStatus() == WaitlistStatus.FULFILLED) {
            throw new BusinessRuleException("Cannot cancel waitlist that has already been converted to a reservation");
        }

        waitlist.setStatus(WaitlistStatus.CANCELLED);
        waitlistRepository.save(waitlist);
        log.info("Cancelled waitlist entry id {}", id);
    }

    @Override
    @Transactional
    public List<RoomDto> getAvailableRoomsForWaitlist(Long id) {
        Waitlist waitlist = waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found with id: " + id));

        List<RoomDto> candidateRooms;
        try {
            candidateRooms = roomClient.getAvailableRooms(waitlist.getRoomCategoryId(), waitlist.getGuestsCount());
        } catch (Exception e) {
            log.warn("Failed to fetch available rooms from room-service: {}", e.getMessage());
            candidateRooms = new ArrayList<>();
        }

        LocalDateTime checkIn = waitlist.getCheckInDate().atTime(14, 0);
        LocalDateTime checkOut = waitlist.getCheckOutDate().atTime(11, 0);

        // Filter out rooms that have active reservation overlaps during the waitlist stay dates
        List<RoomDto> availableRooms = candidateRooms.stream()
                .filter(RoomDto::isActive)
                .filter(room -> {
                    List<Reservation> overlaps = reservationRepository.findOverlappingRoomReservations(
                            room.getId(),
                            checkIn,
                            checkOut,
                            ACTIVE_STATUSES,
                            null
                    );
                    return overlaps.isEmpty();
                })
                .collect(Collectors.toList());

        if (!availableRooms.isEmpty() && waitlist.getStatus() == WaitlistStatus.ACTIVE) {
            waitlist.setStatus(WaitlistStatus.OFFERED);
            waitlist.setOfferedRoomId(availableRooms.get(0).getId());
            waitlistRepository.save(waitlist);
            log.info("Waitlist entry {} transitioned to OFFERED with room {}", id, availableRooms.get(0).getId());
        }

        return availableRooms;
    }

    @Override
    @Transactional
    public WaitlistResponse confirmWaitlist(Long id, Long roomId) {
        Waitlist waitlist = waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found with id: " + id));

        if (waitlist.getStatus() == WaitlistStatus.CANCELLED || waitlist.getStatus() == WaitlistStatus.REJECTED || waitlist.getStatus() == WaitlistStatus.EXPIRED) {
            throw new BusinessRuleException("Cannot confirm waitlist with status: " + waitlist.getStatus());
        }

        if (waitlist.getStatus() == WaitlistStatus.CONVERTED || waitlist.getStatus() == WaitlistStatus.FULFILLED) {
            throw new BusinessRuleException("Waitlist has already been converted to a reservation");
        }

        if (roomId != null) {
            waitlist.setOfferedRoomId(roomId);
        }

        waitlist.setStatus(WaitlistStatus.CONFIRMED);
        Waitlist updated = waitlistRepository.save(waitlist);
        log.info("Confirmed waitlist entry id {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public WaitlistResponse rejectWaitlist(Long id, String reason) {
        Waitlist waitlist = waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found with id: " + id));

        if (waitlist.getStatus() == WaitlistStatus.CONVERTED || waitlist.getStatus() == WaitlistStatus.FULFILLED) {
            throw new BusinessRuleException("Cannot reject waitlist that has already been converted to a reservation");
        }

        waitlist.setStatus(WaitlistStatus.REJECTED);
        waitlist.setRejectionReason(reason != null ? reason : "Declined by guest");
        Waitlist updated = waitlistRepository.save(waitlist);
        log.info("Rejected waitlist entry id {} with reason: {}", id, reason);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public WaitlistResponse expireWaitlist(Long id) {
        Waitlist waitlist = waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found with id: " + id));

        if (waitlist.getStatus() == WaitlistStatus.CONVERTED || waitlist.getStatus() == WaitlistStatus.FULFILLED) {
            throw new BusinessRuleException("Cannot expire waitlist that has already been converted to a reservation");
        }

        waitlist.setStatus(WaitlistStatus.EXPIRED);
        Waitlist updated = waitlistRepository.save(waitlist);
        log.info("Expired waitlist entry id {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse convertToReservation(Long id, Long roomId, String specialRequests) {
        Waitlist waitlist = waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found with id: " + id));

        if (waitlist.getStatus() == WaitlistStatus.CANCELLED || waitlist.getStatus() == WaitlistStatus.REJECTED || waitlist.getStatus() == WaitlistStatus.EXPIRED) {
            throw new BusinessRuleException("Cannot convert waitlist with status: " + waitlist.getStatus());
        }

        if (waitlist.getReservationId() != null || waitlist.getStatus() == WaitlistStatus.CONVERTED || waitlist.getStatus() == WaitlistStatus.FULFILLED) {
            throw new BusinessRuleException("Waitlist has already been converted to reservation ID: " + waitlist.getReservationId());
        }

        Long targetRoomId = roomId != null ? roomId : waitlist.getOfferedRoomId();

        CreateReservationRequest reservationRequest = CreateReservationRequest.builder()
                .guestId(waitlist.getGuestId())
                .resourceType(waitlist.getResourceType())
                .roomCategoryId(waitlist.getRoomCategoryId())
                .roomId(targetRoomId)
                .adults(waitlist.getGuestsCount() != null ? waitlist.getGuestsCount() : 1)
                .children(0)
                .checkInDateTime(waitlist.getCheckInDate().atTime(14, 0))
                .checkOutDateTime(waitlist.getCheckOutDate().atTime(11, 0))
                .specialRequests(specialRequests != null ? specialRequests : "Created from Waitlist #" + waitlist.getId())
                .build();

        ReservationResponse reservationResponse = reservationService.createReservation(reservationRequest);

        waitlist.setStatus(WaitlistStatus.CONVERTED);
        waitlist.setReservationId(reservationResponse.getId());
        if (targetRoomId != null) {
            waitlist.setOfferedRoomId(targetRoomId);
        }
        waitlistRepository.save(waitlist);

        log.info("Successfully converted waitlist {} to reservation {}", waitlist.getId(), reservationResponse.getReservationCode());
        return reservationResponse;
    }

    private WaitlistResponse mapToResponse(Waitlist w) {
        return WaitlistResponse.builder()
                .id(w.getId())
                .guestId(w.getGuestId())
                .roomCategoryId(w.getRoomCategoryId())
                .resourceType(w.getResourceType())
                .checkInDate(w.getCheckInDate())
                .checkOutDate(w.getCheckOutDate())
                .guestsCount(w.getGuestsCount())
                .priority(w.getPriority())
                .status(w.getStatus())
                .offeredRoomId(w.getOfferedRoomId())
                .reservationId(w.getReservationId())
                .rejectionReason(w.getRejectionReason())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}
