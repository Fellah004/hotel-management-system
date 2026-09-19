package com.hms.reportingservice.repository;

import com.hms.reportingservice.entity.OccupancySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OccupancySnapshotRepository extends JpaRepository<OccupancySnapshot, Long> {
    Optional<OccupancySnapshot> findFirstByOrderBySnapshotDateDesc();
    List<OccupancySnapshot> findBySnapshotDateBetween(LocalDate startDate, LocalDate endDate);
    List<OccupancySnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate startDate, LocalDate endDate);
}
