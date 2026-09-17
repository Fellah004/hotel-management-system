package com.hms.staffservice.service.impl;

import com.hms.staffservice.dto.request.ShiftRequest;
import com.hms.staffservice.dto.response.ShiftResponse;
import com.hms.staffservice.entity.Shift;
import com.hms.staffservice.exception.DuplicateResourceException;
import com.hms.staffservice.exception.ResourceNotFoundException;
import com.hms.staffservice.repository.ShiftRepository;
import com.hms.staffservice.repository.StaffRepository;
import com.hms.staffservice.service.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final StaffRepository staffRepository;

    @Override
    @Transactional
    public ShiftResponse createShift(ShiftRequest request) {
        if (shiftRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Shift already exists with name: " + request.getName());
        }

        Shift shift = Shift.builder()
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .assignedStaffIds(request.getAssignedStaffIds() != null ? new HashSet<>(request.getAssignedStaffIds()) : new HashSet<>())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Shift saved = shiftRepository.save(shift);
        log.info("Created shift: {}", saved.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponse getShiftById(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
        return mapToResponse(shift);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getAllShifts() {
        return shiftRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getShiftsByStaffId(Long staffId) {
        return shiftRepository.findShiftsByStaffId(staffId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShiftResponse updateShift(Long id, ShiftRequest request) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));

        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        if (request.getAssignedStaffIds() != null) {
            shift.setAssignedStaffIds(new HashSet<>(request.getAssignedStaffIds()));
        }
        if (request.getActive() != null) {
            shift.setActive(request.getActive());
        }

        Shift updated = shiftRepository.save(shift);
        log.info("Updated shift id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ShiftResponse assignStaffToShift(Long shiftId, Long staffId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + shiftId));
        if (!staffRepository.existsById(staffId)) {
            throw new ResourceNotFoundException("Staff not found with id: " + staffId);
        }

        shift.getAssignedStaffIds().add(staffId);
        Shift updated = shiftRepository.save(shift);
        log.info("Assigned staff {} to shift {}", staffId, shift.getName());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ShiftResponse removeStaffFromShift(Long shiftId, Long staffId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + shiftId));

        shift.getAssignedStaffIds().remove(staffId);
        Shift updated = shiftRepository.save(shift);
        log.info("Removed staff {} from shift {}", staffId, shift.getName());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteShift(Long id) {
        if (!shiftRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shift not found with id: " + id);
        }
        shiftRepository.deleteById(id);
        log.info("Deleted shift id: {}", id);
    }

    private ShiftResponse mapToResponse(Shift s) {
        return ShiftResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .assignedStaffIds(s.getAssignedStaffIds() != null ? new HashSet<>(s.getAssignedStaffIds()) : Set.of())
                .active(s.isActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
