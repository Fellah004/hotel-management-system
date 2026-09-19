package com.hms.roomservice.service;

import com.hms.roomservice.dto.request.CreateAmenityRequest;
import com.hms.roomservice.dto.response.AmenityResponse;

import java.util.List;

public interface AmenityService {
    AmenityResponse createAmenity(CreateAmenityRequest request);
    AmenityResponse getAmenityById(Long id);
    List<AmenityResponse> getAllAmenities();
    AmenityResponse updateAmenity(Long id, CreateAmenityRequest request);
    void deleteAmenity(Long id);
}
