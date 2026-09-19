package com.hms.reservationservice.service;

import com.hms.reservationservice.client.dto.RoomDto;
import com.hms.reservationservice.dto.request.WaitlistRequest;
import com.hms.reservationservice.dto.response.ReservationResponse;
import com.hms.reservationservice.dto.response.WaitlistResponse;

import java.util.List;

public interface WaitlistService {
    WaitlistResponse joinWaitlist(WaitlistRequest request);
    WaitlistResponse getWaitlistById(Long id);
    List<WaitlistResponse> getWaitlistsByGuest(Long guestId);
    List<WaitlistResponse> getAllActiveWaitlists();
    void cancelWaitlist(Long id);

    List<RoomDto> getAvailableRoomsForWaitlist(Long id);
    WaitlistResponse confirmWaitlist(Long id, Long roomId);
    WaitlistResponse rejectWaitlist(Long id, String reason);
    WaitlistResponse expireWaitlist(Long id);
    ReservationResponse convertToReservation(Long id, Long roomId, String specialRequests);
}
