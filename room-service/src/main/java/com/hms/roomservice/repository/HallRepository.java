package com.hms.roomservice.repository;

import com.hms.roomservice.entity.Hall;
import com.hms.roomservice.entity.HallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {
    Optional<Hall> findByHallNumber(String hallNumber);
    boolean existsByHallNumber(String hallNumber);
    List<Hall> findByStatus(HallStatus status);
    List<Hall> findByStatusAndActiveTrue(HallStatus status);
    List<Hall> findByCategoryId(Long categoryId);
    List<Hall> findByActiveTrue();

    @Query("SELECT h FROM Hall h WHERE h.active = true AND h.status = :status AND (:categoryId IS NULL OR h.category.id = :categoryId) AND (:minCapacity IS NULL OR h.theaterCapacity >= :minCapacity)")
    List<Hall> findAvailableHalls(
            @Param("status") HallStatus status,
            @Param("categoryId") Long categoryId,
            @Param("minCapacity") Integer minCapacity
    );
}
