package com.hms.guestservice.service;

import com.hms.guestservice.dto.request.CreateGuestRequest;
import com.hms.guestservice.dto.request.UpdateGuestRequest;
import com.hms.guestservice.dto.response.GuestResponse;

import java.util.List;

public interface GuestService {
    GuestResponse createGuest(CreateGuestRequest request);
    GuestResponse getGuestById(Long id, Long currentUserId, String currentUserRole);
    GuestResponse getGuestByUserId(Long userId);
    GuestResponse getGuestByMemberCode(String memberCode);
    List<GuestResponse> getAllGuests();
    GuestResponse updateGuest(Long id, UpdateGuestRequest request, Long currentUserId, String currentUserRole);
    void deleteGuest(Long id);
}
