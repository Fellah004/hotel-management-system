package com.hms.guestexperienceservice.repository;

import com.hms.guestexperienceservice.entity.RequestStatus;
import com.hms.guestexperienceservice.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByReservationId(Long reservationId);
    List<ServiceRequest> findByGuestId(Long guestId);
    List<ServiceRequest> findByRoomId(Long roomId);
    List<ServiceRequest> findByStatus(RequestStatus status);
    Optional<ServiceRequest> findByRequestCode(String requestCode);
}
