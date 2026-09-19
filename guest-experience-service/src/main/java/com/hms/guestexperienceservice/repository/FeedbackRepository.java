package com.hms.guestexperienceservice.repository;

import com.hms.guestexperienceservice.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByGuestId(Long guestId);
    List<Feedback> findByReservationId(Long reservationId);
}
