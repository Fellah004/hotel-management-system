package com.hms.reservationservice.service.impl;

import com.hms.reservationservice.client.GuestClient;
import com.hms.reservationservice.client.RateClient;
import com.hms.reservationservice.client.RoomClient;
import com.hms.reservationservice.client.dto.*;
import com.hms.reservationservice.dto.request.*;
import com.hms.reservationservice.dto.response.ReservationResponse;
import com.hms.reservationservice.entity.Reservation;
import com.hms.reservationservice.entity.ReservationStatus;
import com.hms.reservationservice.event.publisher.ReservationEventPublisher;
import com.hms.reservationservice.exception.BusinessRuleException;
import com.hms.reservationservice.exception.ForbiddenException;
import com.hms.reservationservice.exception.ResourceNotFoundException;
import com.hms.reservationservice.repository.ReservationRepository;
import com.hms.reservationservice.service.ReservationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final GuestClient guestClient;
    private final RoomClient roomClient;
    private final RateClient rateClient;
    private final ReservationEventPublisher eventPublisher;

    private static final Set<ReservationStatus> ACTIVE_STATUSES = Set.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED,
            ReservationStatus.CHECKED_IN
    );

    @Override
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        if (!request.getCheckOutDateTime().isAfter(request.getCheckInDateTime())) {
            throw new BusinessRuleException("Check-out date/time must be strictly after check-in date/time");
        }

        // Validate guest
        validateGuest(request.getGuestId());

        // Validate double-booking if room is pre-assigned
        if (request.getRoomId() != null) {
            List<Reservation> overlaps = reservationRepository.findOverlappingRoomReservations(
                    request.getRoomId(),
                    request.getCheckInDateTime(),
                    request.getCheckOutDateTime(),
                    ACTIVE_STATUSES,
                    null
            );
            if (!overlaps.isEmpty()) {
                throw new BusinessRuleException("Room " + request.getRoomId() + " is already booked for the selected dates");
            }
        }

        // Calculate nights
        long nights = ChronoUnit.DAYS.between(request.getCheckInDateTime().toLocalDate(), request.getCheckOutDateTime().toLocalDate());
        if (nights <= 0) nights = 1;

        // Obtain dynamic pricing quote
        BigDecimal quotedAmount = getQuotedPrice(request.getRoomCategoryId(), request.getRoomId(),
                request.getResourceType(), request.getCheckInDateTime(), request.getCheckOutDateTime(),
                request.getAdults() + (request.getChildren() != null ? request.getChildren() : 0));

        String reservationCode = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Reservation reservation = Reservation.builder()
                .reservationCode(reservationCode)
                .guestId(request.getGuestId())
                .resourceType(request.getResourceType() != null ? request.getResourceType() : "ROOM")
                .roomId(request.getRoomId())
                .hallId(request.getHallId())
                .roomCategoryId(request.getRoomCategoryId())
                .adults(request.getAdults())
                .children(request.getChildren() != null ? request.getChildren() : 0)
                .checkInDateTime(request.getCheckInDateTime())
                .checkOutDateTime(request.getCheckOutDateTime())
                .nights((int) nights)
                .quotedAmount(quotedAmount)
                .status(ReservationStatus.PENDING)
                .specialRequests(request.getSpecialRequests())
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Created reservation {} for guest {}", saved.getReservationCode(), saved.getGuestId());

        // Publish event to RabbitMQ
        eventPublisher.publishReservationCreated(saved);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, Long currentUserId, String currentUserRole) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        validateOwnership(reservation, currentUserId, currentUserRole);
        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationByCode(String reservationCode, Long currentUserId, String currentUserRole) {
        Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with code: " + reservationCode));

        validateOwnership(reservation, currentUserId, currentUserRole);
        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByGuestId(Long guestId, Long currentUserId, String currentUserRole) {
        return reservationRepository.findByGuestId(guestId).stream()
                .filter(res -> {
                    try {
                        validateOwnership(res, currentUserId, currentUserRole);
                        return true;
                    } catch (ForbiddenException e) {
                        return false;
                    }
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReservationResponse updateReservation(Long id, UpdateReservationRequest request, Long currentUserId, String currentUserRole) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        validateOwnership(reservation, currentUserId, currentUserRole);

        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Cannot modify reservation in status " + reservation.getStatus());
        }

        if (request.getCheckInDateTime() != null) reservation.setCheckInDateTime(request.getCheckInDateTime());
        if (request.getCheckOutDateTime() != null) reservation.setCheckOutDateTime(request.getCheckOutDateTime());
        if (request.getAdults() != null) reservation.setAdults(request.getAdults());
        if (request.getChildren() != null) reservation.setChildren(request.getChildren());
        if (request.getSpecialRequests() != null) reservation.setSpecialRequests(request.getSpecialRequests());

        Reservation updated = reservationRepository.save(reservation);
        log.info("Updated reservation {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long id, String reason, Long currentUserId, String currentUserRole) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        validateOwnership(reservation, currentUserId, currentUserRole);

        if (reservation.getStatus() == ReservationStatus.CHECKED_IN || reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new BusinessRuleException("Cannot cancel an active or completed stay in status " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation updated = reservationRepository.save(reservation);

        // Release room status if reserved
        if (reservation.getRoomId() != null) {
            try {
                roomClient.updateRoomStatus(reservation.getRoomId(), new RoomStatusUpdateDto("AVAILABLE", "Reservation cancelled"));
            } catch (Exception e) {
                log.warn("Could not update room status to AVAILABLE on cancellation: {}", e.getMessage());
            }
        }

        eventPublisher.publishReservationCancelled(updated, reason != null ? reason : "Guest requested cancellation");
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse checkIn(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Check-in is only permitted for CONFIRMED reservations. Current status: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);
        Reservation updated = reservationRepository.save(reservation);

        // Set room status to OCCUPIED
        if (reservation.getRoomId() != null) {
            try {
                roomClient.updateRoomStatus(reservation.getRoomId(), new RoomStatusUpdateDto("OCCUPIED", "Guest checked in"));
            } catch (Exception e) {
                log.warn("Failed to set room status to OCCUPIED: {}", e.getMessage());
            }
        }

        eventPublisher.publishReservationCheckedIn(updated);
        log.info("Guest checked in for reservation {}", reservation.getReservationCode());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse checkOut(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BusinessRuleException("Checkout is only permitted for CHECKED_IN reservations. Current status: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        Reservation updated = reservationRepository.save(reservation);

        // Set room status to DIRTY (triggers Housekeeping)
        if (reservation.getRoomId() != null) {
            try {
                roomClient.updateRoomStatus(reservation.getRoomId(), new RoomStatusUpdateDto("DIRTY", "Guest checkout cleaning required"));
            } catch (Exception e) {
                log.warn("Failed to set room status to DIRTY: {}", e.getMessage());
            }
        }

        eventPublisher.publishReservationCheckedOut(updated);
        log.info("Guest checked out for reservation {}", reservation.getReservationCode());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse markNoShow(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Only CONFIRMED reservations can be marked as NO_SHOW");
        }

        reservation.setStatus(ReservationStatus.NO_SHOW);
        Reservation updated = reservationRepository.save(reservation);

        // Release room
        if (reservation.getRoomId() != null) {
            try {
                roomClient.updateRoomStatus(reservation.getRoomId(), new RoomStatusUpdateDto("AVAILABLE", "Reservation marked NO_SHOW"));
            } catch (Exception e) {
                log.warn("Failed to release room on NO_SHOW: {}", e.getMessage());
            }
        }

        eventPublisher.publishReservationNoShow(updated);
        log.warn("Reservation {} marked as NO_SHOW", reservation.getReservationCode());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse transferRoom(Long id, RoomTransferRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Room transfer is only allowed for CONFIRMED or CHECKED_IN reservations");
        }

        // Check target room availability
        List<Reservation> overlaps = reservationRepository.findOverlappingRoomReservations(
                request.getTargetRoomId(),
                reservation.getCheckInDateTime(),
                reservation.getCheckOutDateTime(),
                ACTIVE_STATUSES,
                reservation.getId()
        );
        if (!overlaps.isEmpty()) {
            throw new BusinessRuleException("Target room " + request.getTargetRoomId() + " is not available for the remaining duration");
        }

        Long oldRoomId = reservation.getRoomId();
        reservation.setRoomId(request.getTargetRoomId());
        Reservation updated = reservationRepository.save(reservation);

        // Release old room (mark DIRTY if guest was checked in, else AVAILABLE)
        if (oldRoomId != null) {
            try {
                String targetStatus = reservation.getStatus() == ReservationStatus.CHECKED_IN ? "DIRTY" : "AVAILABLE";
                roomClient.updateRoomStatus(oldRoomId, new RoomStatusUpdateDto(targetStatus, "Room transfer old room released"));
                roomClient.updateRoomStatus(request.getTargetRoomId(), new RoomStatusUpdateDto("OCCUPIED", "Room transfer assigned"));
            } catch (Exception e) {
                log.warn("Error updating room status during transfer: {}", e.getMessage());
            }
        }

        log.info("Transferred reservation {} from room {} to {}", reservation.getReservationCode(), oldRoomId, request.getTargetRoomId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse upgradeRoom(Long id, RoomUpgradeRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() == ReservationStatus.CHECKED_OUT || reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot upgrade finalized reservation");
        }

        reservation.setRoomCategoryId(request.getTargetCategoryId());
        if (request.getTargetRoomId() != null) {
            reservation.setRoomId(request.getTargetRoomId());
        }

        // Recalculate price
        BigDecimal newPrice = getQuotedPrice(request.getTargetCategoryId(), reservation.getRoomId(),
                reservation.getResourceType(), reservation.getCheckInDateTime(), reservation.getCheckOutDateTime(),
                reservation.getAdults() + reservation.getChildren());
        reservation.setQuotedAmount(newPrice);

        Reservation updated = reservationRepository.save(reservation);
        log.info("Upgraded reservation {} to category {}", reservation.getReservationCode(), request.getTargetCategoryId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse downgradeRoom(Long id, RoomDowngradeRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        reservation.setRoomCategoryId(request.getTargetCategoryId());
        if (request.getTargetRoomId() != null) {
            reservation.setRoomId(request.getTargetRoomId());
        }

        BigDecimal newPrice = getQuotedPrice(request.getTargetCategoryId(), reservation.getRoomId(),
                reservation.getResourceType(), reservation.getCheckInDateTime(), reservation.getCheckOutDateTime(),
                reservation.getAdults() + reservation.getChildren());
        reservation.setQuotedAmount(newPrice);

        Reservation updated = reservationRepository.save(reservation);
        log.info("Downgraded reservation {} to category {}", reservation.getReservationCode(), request.getTargetCategoryId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse earlyCheckIn(Long id, EarlyCheckInRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Early check-in requires a CONFIRMED reservation");
        }

        reservation.setCheckInDateTime(request.getRequestedCheckInTime());
        Reservation updated = reservationRepository.save(reservation);
        log.info("Processed early check-in for reservation {}", reservation.getReservationCode());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse lateCheckout(Long id, LateCheckoutRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BusinessRuleException("Late checkout requires a CHECKED_IN reservation");
        }

        reservation.setCheckOutDateTime(request.getRequestedCheckoutTime());
        Reservation updated = reservationRepository.save(reservation);
        log.info("Processed late checkout for reservation {}", reservation.getReservationCode());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation not found with id: " + id);
        }
        reservationRepository.deleteById(id);
        log.info("Deleted reservation id {}", id);
    }

    @CircuitBreaker(name = "rateClient", fallbackMethod = "fallbackQuotedPrice")
    private BigDecimal getQuotedPrice(Long categoryId, Long roomId, String resourceType, LocalDateTime checkIn, LocalDateTime checkOut, Integer guests) {
        try {
            RateQuoteRequestDto quoteRequest = RateQuoteRequestDto.builder()
                    .categoryId(categoryId)
                    .roomId(roomId)
                    .resourceType(resourceType)
                    .checkInDate(checkIn.toLocalDate())
                    .checkOutDate(checkOut.toLocalDate())
                    .guestsCount(guests)
                    .currentOccupancyPercentage(new BigDecimal("70.00"))
                    .build();

            RateQuoteResponseDto quote = rateClient.calculateQuote(quoteRequest);
            return quote != null && quote.getTotalQuotedAmount() != null
                    ? quote.getTotalQuotedAmount()
                    : new BigDecimal("150.00");
        } catch (Exception e) {
            log.warn("Failed to get dynamic price quote from rate-service: {}", e.getMessage());
            long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
            if (nights <= 0) nights = 1;
            return new BigDecimal("150.00").multiply(BigDecimal.valueOf(nights));
        }
    }

    public BigDecimal fallbackQuotedPrice(Long categoryId, Long roomId, String resourceType, LocalDateTime checkIn, LocalDateTime checkOut, Integer guests, Throwable t) {
        log.warn("Rate service circuit breaker fallback triggered: {}", t.getMessage());
        long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (nights <= 0) nights = 1;
        return new BigDecimal("150.00").multiply(BigDecimal.valueOf(nights));
    }

    private void validateGuest(Long guestId) {
        try {
            GuestDto guest = guestClient.getGuestById(guestId);
            if (guest == null) {
                log.warn("Guest profile {} not found via OpenFeign client", guestId);
            }
        } catch (Exception e) {
            log.warn("Could not reach guest-service or guest not found: {}", e.getMessage());
        }
    }

    private void validateOwnership(Reservation reservation, Long currentUserId, String currentUserRole) {
        if ("GUEST".equalsIgnoreCase(currentUserRole) && currentUserId != null) {
            try {
                GuestDto guest = guestClient.getGuestById(reservation.getGuestId());
                if (guest != null && guest.getUserId() != null && !guest.getUserId().equals(currentUserId)) {
                    throw new ForbiddenException("Access denied: You can only view or manage your own reservations");
                }
            } catch (ForbiddenException fe) {
                throw fe;
            } catch (Exception e) {
                log.warn("Could not verify guest ownership: {}", e.getMessage());
            }
        }
    }

    private ReservationResponse mapToResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .reservationCode(r.getReservationCode())
                .guestId(r.getGuestId())
                .resourceType(r.getResourceType())
                .roomId(r.getRoomId())
                .hallId(r.getHallId())
                .roomCategoryId(r.getRoomCategoryId())
                .adults(r.getAdults())
                .children(r.getChildren())
                .checkInDateTime(r.getCheckInDateTime())
                .checkOutDateTime(r.getCheckOutDateTime())
                .nights(r.getNights())
                .quotedAmount(r.getQuotedAmount())
                .status(r.getStatus())
                .specialRequests(r.getSpecialRequests())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
