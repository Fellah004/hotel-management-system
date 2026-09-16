package com.hms.reservationservice.service.impl;

import com.hms.reservationservice.dto.request.WaitlistRequest;
import com.hms.reservationservice.dto.response.WaitlistResponse;
import com.hms.reservationservice.entity.Waitlist;
import com.hms.reservationservice.entity.WaitlistStatus;
import com.hms.reservationservice.exception.BusinessRuleException;
import com.hms.reservationservice.exception.ResourceNotFoundException;
import com.hms.reservationservice.repository.WaitlistRepository;
import com.hms.reservationservice.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistServiceImpl implements WaitlistService {

    private final WaitlistRepository waitlistRepository;

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

        waitlist.setStatus(WaitlistStatus.CANCELLED);
        waitlistRepository.save(waitlist);
        log.info("Cancelled waitlist entry id {}", id);
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
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}
