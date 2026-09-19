package com.hms.reservationservice.repository;

import com.hms.reservationservice.entity.Waitlist;
import com.hms.reservationservice.entity.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByGuestId(Long guestId);
    List<Waitlist> findByStatusOrderByPriorityDescCreatedAtAsc(WaitlistStatus status);
    List<Waitlist> findByRoomCategoryIdAndStatusOrderByPriorityDescCreatedAtAsc(Long roomCategoryId, WaitlistStatus status);
}
