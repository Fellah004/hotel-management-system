package com.hms.reservationservice.service;

import com.hms.reservationservice.dto.request.WaitlistRequest;
import com.hms.reservationservice.dto.response.WaitlistResponse;

import java.util.List;

public interface WaitlistService {
    WaitlistResponse joinWaitlist(WaitlistRequest request);
    WaitlistResponse getWaitlistById(Long id);
    List<WaitlistResponse> getWaitlistsByGuest(Long guestId);
    List<WaitlistResponse> getAllActiveWaitlists();
    void cancelWaitlist(Long id);
}
