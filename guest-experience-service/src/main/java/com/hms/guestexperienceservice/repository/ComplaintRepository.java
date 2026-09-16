package com.hms.guestexperienceservice.repository;

import com.hms.guestexperienceservice.entity.Complaint;
import com.hms.guestexperienceservice.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByGuestId(Long guestId);
    List<Complaint> findByStatus(ComplaintStatus status);
    Optional<Complaint> findByComplaintCode(String complaintCode);
}
