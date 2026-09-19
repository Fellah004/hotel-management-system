package com.hms.guestservice.service.impl;

import com.hms.guestservice.dto.request.CreateGuestRequest;
import com.hms.guestservice.dto.request.UpdateGuestRequest;
import com.hms.guestservice.dto.response.GuestResponse;
import com.hms.guestservice.entity.Guest;
import com.hms.guestservice.exception.DuplicateResourceException;
import com.hms.guestservice.exception.ForbiddenException;
import com.hms.guestservice.exception.ResourceNotFoundException;
import com.hms.guestservice.repository.GuestRepository;
import com.hms.guestservice.service.GuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;

    @Override
    @Transactional
    public GuestResponse createGuest(CreateGuestRequest request) {
        if (request.getUserId() != null && guestRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new DuplicateResourceException("Guest profile already exists for user ID: " + request.getUserId());
        }
        if (guestRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Guest with email already exists: " + request.getEmail());
        }
        if (guestRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Guest with phone already exists: " + request.getPhone());
        }

        String memberCode = "MEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Guest guest = Guest.builder()
                .userId(request.getUserId())
                .memberCode(memberCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .company(request.getCompany())
                .email(request.getEmail())
                .gender(request.getGender())
                .address(request.getAddress())
                .build();

        Guest saved = guestRepository.save(guest);
        log.info("Created guest profile id: {}, memberCode: {}", saved.getId(), saved.getMemberCode());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GuestResponse getGuestById(Long id, Long currentUserId, String currentUserRole) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));

        // Object-level authorization check: if user is GUEST, ensure userId matches
        if ("GUEST".equalsIgnoreCase(currentUserRole) && currentUserId != null) {
            if (guest.getUserId() == null || !guest.getUserId().equals(currentUserId)) {
                throw new ForbiddenException("Access denied: You can only view your own guest profile");
            }
        }

        return mapToResponse(guest);
    }

    @Override
    @Transactional(readOnly = true)
    public GuestResponse getGuestByUserId(Long userId) {
        Guest guest = guestRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest profile not found for user id: " + userId));
        return mapToResponse(guest);
    }

    @Override
    @Transactional(readOnly = true)
    public GuestResponse getGuestByMemberCode(String memberCode) {
        Guest guest = guestRepository.findByMemberCode(memberCode)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with member code: " + memberCode));
        return mapToResponse(guest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuestResponse> getAllGuests() {
        return guestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GuestResponse updateGuest(Long id, UpdateGuestRequest request, Long currentUserId, String currentUserRole) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));

        if ("GUEST".equalsIgnoreCase(currentUserRole) && currentUserId != null) {
            if (guest.getUserId() == null || !guest.getUserId().equals(currentUserId)) {
                throw new ForbiddenException("Access denied: You can only update your own guest profile");
            }
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(guest.getEmail())) {
            if (guestRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + request.getEmail());
            }
            guest.setEmail(request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().equalsIgnoreCase(guest.getPhone())) {
            if (guestRepository.existsByPhone(request.getPhone())) {
                throw new DuplicateResourceException("Phone already in use: " + request.getPhone());
            }
            guest.setPhone(request.getPhone());
        }

        if (request.getFirstName() != null) guest.setFirstName(request.getFirstName());
        if (request.getLastName() != null) guest.setLastName(request.getLastName());
        if (request.getCompany() != null) guest.setCompany(request.getCompany());
        if (request.getGender() != null) guest.setGender(request.getGender());
        if (request.getAddress() != null) guest.setAddress(request.getAddress());

        Guest updated = guestRepository.save(guest);
        log.info("Updated guest profile id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteGuest(Long id) {
        if (!guestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Guest not found with id: " + id);
        }
        guestRepository.deleteById(id);
        log.info("Deleted guest profile id: {}", id);
    }

    private GuestResponse mapToResponse(Guest guest) {
        return GuestResponse.builder()
                .id(guest.getId())
                .userId(guest.getUserId())
                .memberCode(guest.getMemberCode())
                .firstName(guest.getFirstName())
                .lastName(guest.getLastName())
                .phone(guest.getPhone())
                .company(guest.getCompany())
                .email(guest.getEmail())
                .gender(guest.getGender())
                .address(guest.getAddress())
                .createdAt(guest.getCreatedAt())
                .updatedAt(guest.getUpdatedAt())
                .build();
    }
}
