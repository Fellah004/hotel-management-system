package com.hms.operationsservice.repository;

import com.hms.operationsservice.entity.MaintenanceIssue;
import com.hms.operationsservice.entity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceIssueRepository extends JpaRepository<MaintenanceIssue, Long> {
    List<MaintenanceIssue> findByRoomId(Long roomId);
    List<MaintenanceIssue> findByStatus(MaintenanceStatus status);
    Optional<MaintenanceIssue> findByIssueCode(String issueCode);
}
