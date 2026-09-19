package com.hms.operationsservice.repository;

import com.hms.operationsservice.entity.HousekeepingTask;
import com.hms.operationsservice.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HousekeepingTaskRepository extends JpaRepository<HousekeepingTask, Long> {
    List<HousekeepingTask> findByAssignedStaffId(Long assignedStaffId);
    List<HousekeepingTask> findByAssignedStaffIdAndStatusIn(Long assignedStaffId, List<TaskStatus> statuses);
    List<HousekeepingTask> findByRoomId(Long roomId);
    List<HousekeepingTask> findByStatus(TaskStatus status);
    Optional<HousekeepingTask> findByTaskCode(String taskCode);
}
