package com.hms.roomservice.service;

import com.hms.roomservice.dto.request.CreateRoomCategoryRequest;
import com.hms.roomservice.dto.response.RoomCategoryResponse;

import java.util.List;

public interface RoomCategoryService {
    RoomCategoryResponse createCategory(CreateRoomCategoryRequest request);
    RoomCategoryResponse getCategoryById(Long id);
    List<RoomCategoryResponse> getAllCategories();
    RoomCategoryResponse updateCategory(Long id, CreateRoomCategoryRequest request);
    void deleteCategory(Long id);
}
