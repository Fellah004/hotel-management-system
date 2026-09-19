package com.hms.reservationservice.repository;

import com.hms.reservationservice.entity.Reservation;
import com.hms.reservationservice.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByReservationCode(String reservationCode);
    boolean existsByReservationCode(String reservationCode);
    List<Reservation> findByGuestId(Long guestId);
    List<Reservation> findByRoomId(Long roomId);
    List<Reservation> findByHallId(Long hallId);
    List<Reservation> findByStatus(ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.roomId = :roomId AND r.status IN :activeStatuses AND r.checkInDateTime < :checkOut AND r.checkOutDateTime > :checkIn AND (:excludeReservationId IS NULL OR r.id != :excludeReservationId)")
    List<Reservation> findOverlappingRoomReservations(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut,
            @Param("activeStatuses") Set<ReservationStatus> activeStatuses,
            @Param("excludeReservationId") Long excludeReservationId
    );

    @Query("SELECT r FROM Reservation r WHERE r.hallId = :hallId AND r.status IN :activeStatuses AND r.checkInDateTime < :checkOut AND r.checkOutDateTime > :checkIn AND (:excludeReservationId IS NULL OR r.id != :excludeReservationId)")
    List<Reservation> findOverlappingHallReservations(
            @Param("hallId") Long hallId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut,
            @Param("activeStatuses") Set<ReservationStatus> activeStatuses,
            @Param("excludeReservationId") Long excludeReservationId
    );
}
