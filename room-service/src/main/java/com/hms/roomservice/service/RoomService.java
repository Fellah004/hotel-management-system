package com.hms.roomservice.service;

import com.hms.roomservice.dto.request.CreateRoomRequest;
import com.hms.roomservice.dto.request.RoomStatusUpdateRequest;
import com.hms.roomservice.dto.request.UpdateRoomRequest;
import com.hms.roomservice.dto.response.RoomResponse;
import com.hms.roomservice.entity.ResourceType;
import com.hms.roomservice.entity.RoomStatus;

import java.util.List;

public interface RoomService {
    RoomResponse createRoom(CreateRoomRequest request);
    RoomResponse getRoomById(Long id);
    RoomResponse getRoomByNumber(String roomNumber);
    List<RoomResponse> getAllRooms();
    List<RoomResponse> getAvailableRooms(Long categoryId, Integer minCapacity, ResourceType resourceType);
    List<RoomResponse> getRoomsByStatus(RoomStatus status);
    RoomResponse updateRoom(Long id, UpdateRoomRequest request);
    RoomResponse updateRoomStatus(Long id, RoomStatusUpdateRequest request, String userRole);
    void deleteRoom(Long id);
}
