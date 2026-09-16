package com.hms.reservationservice.service;

import com.hms.reservationservice.dto.request.*;
import com.hms.reservationservice.dto.response.ReservationResponse;

import java.util.List;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);
    ReservationResponse getReservationById(Long id, Long currentUserId, String currentUserRole);
    ReservationResponse getReservationByCode(String reservationCode, Long currentUserId, String currentUserRole);
    List<ReservationResponse> getReservationsByGuestId(Long guestId, Long currentUserId, String currentUserRole);
    List<ReservationResponse> getReservationsByRoomId(Long roomId);
    List<ReservationResponse> getAllReservations();

    ReservationResponse updateReservation(Long id, UpdateReservationRequest request, Long currentUserId, String currentUserRole);
    ReservationResponse cancelReservation(Long id, String reason, Long currentUserId, String currentUserRole);
    ReservationResponse checkIn(Long id);
    ReservationResponse checkOut(Long id);
    ReservationResponse markNoShow(Long id);

    ReservationResponse transferRoom(Long id, RoomTransferRequest request);
    ReservationResponse upgradeRoom(Long id, RoomUpgradeRequest request);
    ReservationResponse downgradeRoom(Long id, RoomDowngradeRequest request);
    ReservationResponse earlyCheckIn(Long id, EarlyCheckInRequest request);
    ReservationResponse lateCheckout(Long id, LateCheckoutRequest request);

    void deleteReservation(Long id);
}
