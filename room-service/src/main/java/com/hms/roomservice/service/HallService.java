package com.hms.roomservice.service;

import com.hms.roomservice.dto.request.CreateHallRequest;
import com.hms.roomservice.dto.request.HallStatusUpdateRequest;
import com.hms.roomservice.dto.request.UpdateHallRequest;
import com.hms.roomservice.dto.response.HallResponse;
import com.hms.roomservice.entity.HallStatus;

import java.util.List;

public interface HallService {
    HallResponse createHall(CreateHallRequest request);
    HallResponse getHallById(Long id);
    HallResponse getHallByNumber(String hallNumber);
    List<HallResponse> getAllHalls();
    List<HallResponse> getAllHalls(boolean includeInactive);
    List<HallResponse> getAvailableHalls(Long categoryId, Integer minCapacity);
    List<HallResponse> getHallsByStatus(HallStatus status);
    List<HallResponse> getHallsByStatus(HallStatus status, boolean includeInactive);
    HallResponse updateHall(Long id, UpdateHallRequest request);
    HallResponse updateHallStatus(Long id, HallStatusUpdateRequest request);
    HallResponse activateHall(Long id);
    HallResponse deactivateHall(Long id);
    void deleteHall(Long id);
}
