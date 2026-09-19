package com.hms.roomservice.service;

import com.hms.roomservice.dto.request.CreateHallCategoryRequest;
import com.hms.roomservice.dto.request.UpdateHallCategoryRequest;
import com.hms.roomservice.dto.response.HallCategoryResponse;

import java.util.List;

public interface HallCategoryService {
    HallCategoryResponse createCategory(CreateHallCategoryRequest request);
    HallCategoryResponse getCategoryById(Long id);
    List<HallCategoryResponse> getAllCategories();
    HallCategoryResponse updateCategory(Long id, UpdateHallCategoryRequest request);
    void deleteCategory(Long id);
}
