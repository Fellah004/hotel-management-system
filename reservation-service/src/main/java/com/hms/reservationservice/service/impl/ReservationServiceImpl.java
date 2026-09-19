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
    private final com.hms.reservationservice.client.BillingClient billingClient;
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

        String resourceType = request.getResourceType() != null ? request.getResourceType().toUpperCase() : "ROOM";

        // Validate occupancy and special requests (e.g. Single Room rules & Extra Bed requirement)
        validateOccupancy(request, resourceType);

        // Validate double-booking and required category based on resource type
        if ("HALL".equalsIgnoreCase(resourceType)) {
            if (request.getHallId() == null && request.getHallCategoryId() == null) {
                throw new BusinessRuleException("Either hallId or hallCategoryId is required for a Hall reservation");
            }
            if (request.getHallId() != null) {
                List<Reservation> overlaps = reservationRepository.findOverlappingHallReservations(
                        request.getHallId(),
                        request.getCheckInDateTime(),
                        request.getCheckOutDateTime(),
                        ACTIVE_STATUSES,
                        null
                );
                if (!overlaps.isEmpty()) {
                    throw new BusinessRuleException("Hall " + request.getHallId() + " is already booked for the selected dates");
                }
            }
        } else {
            if (request.getRoomId() == null && request.getRoomCategoryId() == null) {
                throw new BusinessRuleException("Either roomId or roomCategoryId is required for a Room reservation");
            }
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
        }

        // Calculate nights
        long nights = ChronoUnit.DAYS.between(request.getCheckInDateTime().toLocalDate(), request.getCheckOutDateTime().toLocalDate());
        if (nights <= 0) nights = 1;

        // Obtain dynamic pricing quote
        Long categoryId = "HALL".equalsIgnoreCase(resourceType) ? request.getHallCategoryId() : request.getRoomCategoryId();
        Long resourceId = "HALL".equalsIgnoreCase(resourceType) ? request.getHallId() : request.getRoomId();

        BigDecimal quotedAmount = getQuotedPrice(categoryId, resourceId,
                resourceType, request.getCheckInDateTime(), request.getCheckOutDateTime(),
                request.getAdults() + (request.getChildren() != null ? request.getChildren() : 0));

        String reservationCode = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Reservation reservation = Reservation.builder()
                .reservationCode(reservationCode)
                .guestId(request.getGuestId())
                .resourceType(resourceType)
                .roomId(request.getRoomId())
                .hallId(request.getHallId())
                .roomCategoryId(request.getRoomCategoryId())
                .hallCategoryId(request.getHallCategoryId())
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
    public ReservationResponse confirmReservation(Long id, Long currentUserId, String currentUserRole) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        validateOwnership(reservation, currentUserId, currentUserRole);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING reservations can be confirmed. Current status: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation updated = reservationRepository.save(reservation);

        // Update room status to BOOKED if room is assigned
        if (reservation.getRoomId() != null) {
            try {
                roomClient.updateRoomStatus(reservation.getRoomId(), new RoomStatusUpdateDto("BOOKED", "Reservation confirmed: " + reservation.getReservationCode()));
            } catch (Exception e) {
                log.warn("Could not update room status to BOOKED on confirmation: {}", e.getMessage());
            }
        }

        // Update hall status to BOOKED if hall is assigned
        if (reservation.getHallId() != null) {
            try {
                roomClient.updateHallStatus(reservation.getHallId(), java.util.Map.of("status", "BOOKED"));
            } catch (Exception e) {
                log.warn("Could not update hall status to BOOKED on confirmation: {}", e.getMessage());
            }
        }

        log.info("Confirmed reservation {}", reservation.getReservationCode());
        eventPublisher.publishReservationConfirmed(updated);
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

        // Release hall status if reserved
        if (reservation.getHallId() != null) {
            try {
                roomClient.updateHallStatus(reservation.getHallId(), java.util.Map.of("status", "AVAILABLE"));
            } catch (Exception e) {
                log.warn("Could not update hall status to AVAILABLE on cancellation: {}", e.getMessage());
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

        // Auto-create pending bill in billing-service upon check-in
        try {
            java.util.Map<String, Object> billRequest = new java.util.HashMap<>();
            billRequest.put("reservationId", updated.getId());
            billRequest.put("guestId", updated.getGuestId());
            billRequest.put("roomId", updated.getRoomId());
            billRequest.put("hallId", updated.getHallId());
            if (updated.getCheckInDateTime() != null) {
                billRequest.put("checkInDate", updated.getCheckInDateTime().toLocalDate().toString());
            }
            if (updated.getCheckOutDateTime() != null) {
                billRequest.put("checkOutDate", updated.getCheckOutDateTime().toLocalDate().toString());
            }
            billingClient.createBill(billRequest);
            log.info("Auto-created initial pending bill for reservation {}", updated.getReservationCode());
        } catch (Exception e) {
            log.warn("Could not auto-create bill on check-in for reservation {}: {}", updated.getReservationCode(), e.getMessage());
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

        Long currentCategoryId = reservation.getRoomCategoryId();
        Long targetCategoryId = request.getTargetCategoryId();

        if (currentCategoryId == null && reservation.getRoomId() != null) {
            try {
                RoomDto currentRoom = roomClient.getRoomById(reservation.getRoomId());
                currentCategoryId = extractCategoryId(currentRoom);
            } catch (Exception e) {
                log.warn("Could not determine current category from room {}: {}", reservation.getRoomId(), e.getMessage());
            }
        }

        // Validate that target category is strictly higher tier than current category
        validateCategoryUpgrade(currentCategoryId, targetCategoryId);

        Long oldRoomId = reservation.getRoomId();
        Long newRoomId = request.getTargetRoomId();

        // Validate target room if specified
        if (newRoomId != null) {
            validateTargetRoom(newRoomId, targetCategoryId, reservation);
            reservation.setRoomId(newRoomId);
        } else if (oldRoomId != null) {
            // Category changed without a specific target room assigned yet, clear old room allocation
            reservation.setRoomId(null);
        }

        reservation.setRoomCategoryId(targetCategoryId);

        // Manage room statuses if reservation is active
        if (oldRoomId != null && !oldRoomId.equals(newRoomId) && (reservation.getStatus() == ReservationStatus.CONFIRMED || reservation.getStatus() == ReservationStatus.CHECKED_IN)) {
            try {
                String releaseStatus = reservation.getStatus() == ReservationStatus.CHECKED_IN ? "DIRTY" : "AVAILABLE";
                roomClient.updateRoomStatus(oldRoomId, new RoomStatusUpdateDto(releaseStatus, "Room released due to upgrade"));
            } catch (Exception e) {
                log.warn("Failed to release old room on upgrade: {}", e.getMessage());
            }
        }

        if (newRoomId != null && !newRoomId.equals(oldRoomId) && (reservation.getStatus() == ReservationStatus.CONFIRMED || reservation.getStatus() == ReservationStatus.CHECKED_IN)) {
            try {
                String targetStatus = reservation.getStatus() == ReservationStatus.CHECKED_IN ? "OCCUPIED" : "BOOKED";
                roomClient.updateRoomStatus(newRoomId, new RoomStatusUpdateDto(targetStatus, "Room allocated via upgrade"));
            } catch (Exception e) {
                log.warn("Failed to update new room status on upgrade: {}", e.getMessage());
            }
        }

        // Recalculate price
        BigDecimal newPrice = getQuotedPrice(targetCategoryId, reservation.getRoomId(),
                reservation.getResourceType(), reservation.getCheckInDateTime(), reservation.getCheckOutDateTime(),
                reservation.getAdults() + reservation.getChildren());
        reservation.setQuotedAmount(newPrice);

        Reservation updated = reservationRepository.save(reservation);
        log.info("Upgraded reservation {} to category {}", reservation.getReservationCode(), targetCategoryId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse downgradeRoom(Long id, RoomDowngradeRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() == ReservationStatus.CHECKED_OUT || reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot downgrade finalized reservation");
        }

        Long currentCategoryId = reservation.getRoomCategoryId();
        Long targetCategoryId = request.getTargetCategoryId();

        if (currentCategoryId == null && reservation.getRoomId() != null) {
            try {
                RoomDto currentRoom = roomClient.getRoomById(reservation.getRoomId());
                currentCategoryId = extractCategoryId(currentRoom);
            } catch (Exception e) {
                log.warn("Could not determine current category from room {}: {}", reservation.getRoomId(), e.getMessage());
            }
        }

        // Validate that target category is strictly lower tier than current category
        validateCategoryDowngrade(currentCategoryId, targetCategoryId);

        Long oldRoomId = reservation.getRoomId();
        Long newRoomId = request.getTargetRoomId();

        // Validate target room if specified
        if (newRoomId != null) {
            validateTargetRoom(newRoomId, targetCategoryId, reservation);
            reservation.setRoomId(newRoomId);
        } else if (oldRoomId != null) {
            // Category changed without a specific target room assigned yet, clear old room allocation
            reservation.setRoomId(null);
        }

        reservation.setRoomCategoryId(targetCategoryId);

        // Manage room statuses if reservation is active
        if (oldRoomId != null && !oldRoomId.equals(newRoomId) && (reservation.getStatus() == ReservationStatus.CONFIRMED || reservation.getStatus() == ReservationStatus.CHECKED_IN)) {
            try {
                String releaseStatus = reservation.getStatus() == ReservationStatus.CHECKED_IN ? "DIRTY" : "AVAILABLE";
                roomClient.updateRoomStatus(oldRoomId, new RoomStatusUpdateDto(releaseStatus, "Room released due to downgrade"));
            } catch (Exception e) {
                log.warn("Failed to release old room on downgrade: {}", e.getMessage());
            }
        }

        if (newRoomId != null && !newRoomId.equals(oldRoomId) && (reservation.getStatus() == ReservationStatus.CONFIRMED || reservation.getStatus() == ReservationStatus.CHECKED_IN)) {
            try {
                String targetStatus = reservation.getStatus() == ReservationStatus.CHECKED_IN ? "OCCUPIED" : "BOOKED";
                roomClient.updateRoomStatus(newRoomId, new RoomStatusUpdateDto(targetStatus, "Room allocated via downgrade"));
            } catch (Exception e) {
                log.warn("Failed to update new room status on downgrade: {}", e.getMessage());
            }
        }

        BigDecimal newPrice = getQuotedPrice(targetCategoryId, reservation.getRoomId(),
                reservation.getResourceType(), reservation.getCheckInDateTime(), reservation.getCheckOutDateTime(),
                reservation.getAdults() + reservation.getChildren());
        reservation.setQuotedAmount(newPrice);

        Reservation updated = reservationRepository.save(reservation);
        log.info("Downgraded reservation {} to category {}", reservation.getReservationCode(), targetCategoryId);
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
    @Transactional(readOnly = true)
    public RateQuoteResponseDto getPriceBreakdown(Long id, Long currentUserId, String currentUserRole) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        validateOwnership(reservation, currentUserId, currentUserRole);

        Long categoryId = "HALL".equalsIgnoreCase(reservation.getResourceType())
                ? reservation.getHallCategoryId()
                : reservation.getRoomCategoryId();
        Long resourceId = "HALL".equalsIgnoreCase(reservation.getResourceType())
                ? reservation.getHallId()
                : reservation.getRoomId();
        int guests = (reservation.getAdults() != null ? reservation.getAdults() : 1)
                + (reservation.getChildren() != null ? reservation.getChildren() : 0);

        RateQuoteRequestDto quoteRequest = RateQuoteRequestDto.builder()
                .categoryId(categoryId)
                .roomId(resourceId)
                .resourceType(reservation.getResourceType())
                .checkInDate(reservation.getCheckInDateTime().toLocalDate())
                .checkOutDate(reservation.getCheckOutDateTime().toLocalDate())
                .guestsCount(guests)
                .currentOccupancyPercentage(new BigDecimal("70.00"))
                .build();

        try {
            RateQuoteResponseDto quote = rateClient.calculateQuote(quoteRequest);
            if (quote != null) {
                return quote;
            }
        } catch (Exception e) {
            log.warn("Could not retrieve price breakdown from rate-service for reservation {}: {}", id, e.getMessage());
        }

        // Fallback breakdown
        long nights = reservation.getNights() > 0 ? reservation.getNights() : 1;
        BigDecimal total = reservation.getQuotedAmount() != null ? reservation.getQuotedAmount() : new BigDecimal("100.00").multiply(BigDecimal.valueOf(nights));
        BigDecimal basePerNight = total.divide(BigDecimal.valueOf(nights), 2, java.math.RoundingMode.HALF_UP);

        return RateQuoteResponseDto.builder()
                .categoryId(categoryId)
                .roomId(resourceId)
                .resourceType(reservation.getResourceType())
                .checkInDate(reservation.getCheckInDateTime().toLocalDate())
                .checkOutDate(reservation.getCheckOutDateTime().toLocalDate())
                .numberOfNights((int) nights)
                .guestsCount(guests)
                .basePricePerNight(basePerNight)
                .totalBasePrice(total)
                .totalDynamicAdjustment(BigDecimal.ZERO)
                .totalQuotedAmount(total)
                .dailyBreakdown(List.of())
                .build();
    }

    @Override
    public RateQuoteResponseDto previewQuote(CreateReservationRequest request) {
        if (!request.getCheckOutDateTime().isAfter(request.getCheckInDateTime())) {
            throw new BusinessRuleException("Check-out date/time must be strictly after check-in date/time");
        }

        String resourceType = request.getResourceType() != null ? request.getResourceType().toUpperCase() : "ROOM";
        Long categoryId = "HALL".equalsIgnoreCase(resourceType) ? request.getHallCategoryId() : request.getRoomCategoryId();
        Long resourceId = "HALL".equalsIgnoreCase(resourceType) ? request.getHallId() : request.getRoomId();
        int guests = (request.getAdults() != null ? request.getAdults() : 1) + (request.getChildren() != null ? request.getChildren() : 0);

        RateQuoteRequestDto quoteRequest = RateQuoteRequestDto.builder()
                .categoryId(categoryId)
                .roomId(resourceId)
                .resourceType(resourceType)
                .checkInDate(request.getCheckInDateTime().toLocalDate())
                .checkOutDate(request.getCheckOutDateTime().toLocalDate())
                .guestsCount(guests)
                .currentOccupancyPercentage(new BigDecimal("70.00"))
                .build();

        try {
            RateQuoteResponseDto quote = rateClient.calculateQuote(quoteRequest);
            if (quote != null) {
                return quote;
            }
        } catch (Exception e) {
            log.warn("Could not calculate quote preview from rate-service: {}", e.getMessage());
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckInDateTime().toLocalDate(), request.getCheckOutDateTime().toLocalDate());
        if (nights <= 0) nights = 1;
        BigDecimal dynamicBase = resolveDynamicBasePrice(categoryId, resourceId, resourceType, nights);
        BigDecimal perNight = dynamicBase.divide(BigDecimal.valueOf(nights), 2, java.math.RoundingMode.HALF_UP);

        return RateQuoteResponseDto.builder()
                .categoryId(categoryId)
                .roomId(resourceId)
                .resourceType(resourceType)
                .checkInDate(request.getCheckInDateTime().toLocalDate())
                .checkOutDate(request.getCheckOutDateTime().toLocalDate())
                .numberOfNights((int) nights)
                .guestsCount(guests)
                .basePricePerNight(perNight)
                .totalBasePrice(dynamicBase)
                .totalDynamicAdjustment(BigDecimal.ZERO)
                .totalQuotedAmount(dynamicBase)
                .dailyBreakdown(List.of())
                .build();
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
        long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (nights <= 0) nights = 1;

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
            if (quote != null && quote.getTotalQuotedAmount() != null) {
                return quote.getTotalQuotedAmount();
            }
        } catch (Exception e) {
            log.warn("Failed to get dynamic price quote from rate-service: {}. Falling back to dynamic category base price.", e.getMessage());
        }

        return resolveDynamicBasePrice(categoryId, roomId, resourceType, nights);
    }

    public BigDecimal fallbackQuotedPrice(Long categoryId, Long roomId, String resourceType, LocalDateTime checkIn, LocalDateTime checkOut, Integer guests, Throwable t) {
        log.warn("Rate service circuit breaker fallback triggered: {}. Resolving dynamic base rate.", t.getMessage());
        long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (nights <= 0) nights = 1;
        return resolveDynamicBasePrice(categoryId, roomId, resourceType, nights);
    }

    private BigDecimal resolveDynamicBasePrice(Long categoryId, Long roomId, String resourceType, long nights) {
        BigDecimal dailyRate = null;

        if (roomId != null && "ROOM".equalsIgnoreCase(resourceType)) {
            try {
                RoomDto room = roomClient.getRoomById(roomId);
                if (room != null && room.getPricePerNight() != null) {
                    dailyRate = room.getPricePerNight();
                }
            } catch (Exception ignored) {}
        }

        if (dailyRate == null && categoryId != null) {
            if ("HALL".equalsIgnoreCase(resourceType)) {
                try {
                    java.util.Map<String, Object> hallCat = roomClient.getHallCategoryById(categoryId);
                    if (hallCat != null && hallCat.get("basePricePerDay") != null) {
                        dailyRate = new BigDecimal(hallCat.get("basePricePerDay").toString());
                    }
                } catch (Exception ignored) {}
            } else {
                dailyRate = getCategoryBasePrice(categoryId);
            }
        }

        if (dailyRate == null) {
            dailyRate = new BigDecimal("100.00");
        }

        return dailyRate.multiply(BigDecimal.valueOf(nights));
    }

    private void validateOccupancy(CreateReservationRequest request, String resourceType) {
        if (!"ROOM".equalsIgnoreCase(resourceType)) {
            return;
        }

        int adults = request.getAdults() != null ? request.getAdults() : 0;
        int children = request.getChildren() != null ? request.getChildren() : 0;
        int totalGuests = adults + children;
        String specialRequests = request.getSpecialRequests() != null ? request.getSpecialRequests().toLowerCase() : "";
        boolean hasExtraBedRequest = specialRequests.contains("extra bed");

        String categoryName = null;
        Integer maxOccupancy = null;

        try {
            if (request.getRoomCategoryId() != null) {
                java.util.Map<String, Object> catMap = roomClient.getRoomCategoryById(request.getRoomCategoryId());
                if (catMap != null) {
                    if (catMap.get("name") != null) {
                        categoryName = catMap.get("name").toString();
                    }
                    if (catMap.get("maxOccupancy") != null) {
                        maxOccupancy = Integer.parseInt(catMap.get("maxOccupancy").toString());
                    }
                }
            } else if (request.getRoomId() != null) {
                RoomDto room = roomClient.getRoomById(request.getRoomId());
                if (room != null) {
                    if (room.getCapacity() != null) {
                        maxOccupancy = room.getCapacity();
                    }
                    if (room.getCategory() instanceof java.util.Map) {
                        java.util.Map<?, ?> catMap = (java.util.Map<?, ?>) room.getCategory();
                        if (catMap.get("name") != null) {
                            categoryName = catMap.get("name").toString();
                        }
                        if (catMap.get("maxOccupancy") != null) {
                            maxOccupancy = Integer.parseInt(catMap.get("maxOccupancy").toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not retrieve room category details for occupancy validation: {}", e.getMessage());
        }

        boolean isSingleRoom = false;
        if (categoryName != null && categoryName.toUpperCase().contains("SINGLE")) {
            isSingleRoom = true;
        } else if (maxOccupancy != null && maxOccupancy == 1) {
            isSingleRoom = true;
        }

        if (isSingleRoom) {
            // Default single room allows strictly 1 adult and 0 children
            if (adults > 1 || children > 0) {
                if (!hasExtraBedRequest) {
                    throw new BusinessRuleException("Single Room allows only 1 adult and 0 children by default. Additional guest requires a valid special request: 'Extra Bed Required'.");
                }
                if (totalGuests > 2) {
                    throw new BusinessRuleException("Single Room with Extra Bed can accommodate a maximum of 2 guests.");
                }
            }
        } else if (maxOccupancy != null) {
            if (totalGuests > maxOccupancy) {
                if (!hasExtraBedRequest) {
                    throw new BusinessRuleException("Requested guest count (" + totalGuests + ") exceeds standard occupancy (" + maxOccupancy + ") for this room. An 'Extra Bed Required' special request is required for additional guests.");
                }
                if (totalGuests > maxOccupancy + 1) {
                    throw new BusinessRuleException("Requested room with Extra Bed exceeds maximum permissible occupancy of " + (maxOccupancy + 1) + " guests.");
                }
            }
        }
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

    private void validateCategoryUpgrade(Long currentCategoryId, Long targetCategoryId) {
        if (currentCategoryId == null || targetCategoryId == null) {
            return;
        }
        if (currentCategoryId.equals(targetCategoryId)) {
            String name = getCategoryNameById(currentCategoryId);
            throw new BusinessRuleException("Invalid Upgrade: Target category must be strictly higher than current category. Both are '" + name + "' (ID: " + currentCategoryId + ").");
        }

        String currentName = getCategoryNameById(currentCategoryId);
        String targetName = getCategoryNameById(targetCategoryId);
        BigDecimal currentPrice = getCategoryBasePrice(currentCategoryId);
        BigDecimal targetPrice = getCategoryBasePrice(targetCategoryId);

        int currentRank = getCategoryRank(currentName, currentPrice, currentCategoryId);
        int targetRank = getCategoryRank(targetName, targetPrice, targetCategoryId);

        if (targetRank <= currentRank) {
            throw new BusinessRuleException(String.format(
                    "Invalid Upgrade: Target category '%s' (Tier %d) must be strictly higher than current category '%s' (Tier %d).",
                    targetName, targetRank, currentName, currentRank
            ));
        }
    }

    private void validateCategoryDowngrade(Long currentCategoryId, Long targetCategoryId) {
        if (currentCategoryId == null || targetCategoryId == null) {
            return;
        }
        if (currentCategoryId.equals(targetCategoryId)) {
            String name = getCategoryNameById(currentCategoryId);
            throw new BusinessRuleException("Invalid Downgrade: Target category must be strictly lower than current category. Both are '" + name + "' (ID: " + currentCategoryId + ").");
        }

        String currentName = getCategoryNameById(currentCategoryId);
        String targetName = getCategoryNameById(targetCategoryId);
        BigDecimal currentPrice = getCategoryBasePrice(currentCategoryId);
        BigDecimal targetPrice = getCategoryBasePrice(targetCategoryId);

        int currentRank = getCategoryRank(currentName, currentPrice, currentCategoryId);
        int targetRank = getCategoryRank(targetName, targetPrice, targetCategoryId);

        if (targetRank >= currentRank) {
            throw new BusinessRuleException(String.format(
                    "Invalid Downgrade: Target category '%s' (Tier %d) must be strictly lower than current category '%s' (Tier %d).",
                    targetName, targetRank, currentName, currentRank
            ));
        }
    }

    private void validateTargetRoom(Long targetRoomId, Long targetCategoryId, Reservation reservation) {
        RoomDto room;
        try {
            room = roomClient.getRoomById(targetRoomId);
        } catch (Exception e) {
            throw new BusinessRuleException("Target room ID " + targetRoomId + " could not be found or verified: " + e.getMessage());
        }

        if (room == null) {
            throw new BusinessRuleException("Target room ID " + targetRoomId + " does not exist");
        }

        if (!room.isActive()) {
            throw new BusinessRuleException("Target room ID " + targetRoomId + " (Room " + room.getRoomNumber() + ") is inactive and cannot be assigned");
        }

        Long actualCategoryId = extractCategoryId(room);
        String actualCategoryName = extractCategoryName(room);
        String targetCategoryName = getCategoryNameById(targetCategoryId);

        if (actualCategoryId != null && !actualCategoryId.equals(targetCategoryId)) {
            throw new BusinessRuleException(String.format(
                    "Selected room ID %d (Room %s) belongs to category '%s' (ID: %d), which does not match the targeted category '%s' (ID: %d)",
                    targetRoomId,
                    room.getRoomNumber() != null ? room.getRoomNumber() : targetRoomId.toString(),
                    actualCategoryName != null ? actualCategoryName : "UNKNOWN",
                    actualCategoryId,
                    targetCategoryName,
                    targetCategoryId
            ));
        }

        // Check date overlap
        List<Reservation> overlaps = reservationRepository.findOverlappingRoomReservations(
                targetRoomId,
                reservation.getCheckInDateTime(),
                reservation.getCheckOutDateTime(),
                ACTIVE_STATUSES,
                reservation.getId()
        );
        if (!overlaps.isEmpty()) {
            throw new BusinessRuleException("Target room ID " + targetRoomId + " (Room " + room.getRoomNumber() + ") is already booked for the selected dates");
        }
    }

    private Long extractCategoryId(RoomDto room) {
        if (room == null || room.getCategory() == null) return null;
        if (room.getCategory() instanceof java.util.Map) {
            Object idObj = ((java.util.Map<?, ?>) room.getCategory()).get("id");
            if (idObj != null) {
                try {
                    return Long.parseLong(idObj.toString());
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private String extractCategoryName(RoomDto room) {
        if (room == null || room.getCategory() == null) return null;
        if (room.getCategory() instanceof java.util.Map) {
            Object nameObj = ((java.util.Map<?, ?>) room.getCategory()).get("name");
            if (nameObj != null) {
                return nameObj.toString();
            }
        }
        return null;
    }

    private String getCategoryNameById(Long categoryId) {
        if (categoryId == null) return "Unknown";
        try {
            java.util.Map<String, Object> cat = roomClient.getRoomCategoryById(categoryId);
            if (cat != null && cat.get("name") != null) {
                return cat.get("name").toString();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch category name for id {}: {}", categoryId, e.getMessage());
        }
        return "Category " + categoryId;
    }

    private BigDecimal getCategoryBasePrice(Long categoryId) {
        if (categoryId == null) return null;
        try {
            java.util.Map<String, Object> cat = roomClient.getRoomCategoryById(categoryId);
            if (cat != null && cat.get("basePrice") != null) {
                return new BigDecimal(cat.get("basePrice").toString());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch category basePrice for id {}: {}", categoryId, e.getMessage());
        }
        return null;
    }

    private int getCategoryRank(String name, BigDecimal basePrice, Long categoryId) {
        if (name != null) {
            String upper = name.toUpperCase().replace(" ", "_");
            if (upper.contains("SINGLE")) return 1;
            if (upper.contains("DOUBLE")) return 2;
            if (upper.contains("STANDARD")) return 3;
            if (upper.contains("DELUXE")) return 4;
            if (upper.contains("SUITE") && !upper.contains("PRESIDENTIAL")) return 5;
            if (upper.contains("PRESIDENTIAL")) return 6;
        }
        if (basePrice != null) {
            return basePrice.intValue();
        }
        return categoryId != null ? categoryId.intValue() : 0;
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
                .hallCategoryId(r.getHallCategoryId())
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
