package com.hms.roomservice.repository;

import com.hms.roomservice.entity.ResourceType;
import com.hms.roomservice.entity.Room;
import com.hms.roomservice.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);
    boolean existsByRoomNumber(String roomNumber);
    List<Room> findByStatus(RoomStatus status);
    List<Room> findByResourceType(ResourceType resourceType);
    List<Room> findByCategoryId(Long categoryId);
    List<Room> findByActiveTrue();

    @Query("SELECT r FROM Room r WHERE r.active = true AND r.status = :status AND (:categoryId IS NULL OR r.category.id = :categoryId) AND (:minCapacity IS NULL OR r.capacity >= :minCapacity) AND (:resourceType IS NULL OR r.resourceType = :resourceType)")
    List<Room> findAvailableRooms(
            @Param("status") RoomStatus status,
            @Param("categoryId") Long categoryId,
            @Param("minCapacity") Integer minCapacity,
            @Param("resourceType") ResourceType resourceType
    );
}
