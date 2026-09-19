package com.hms.reportingservice.client;

import com.hms.reportingservice.client.dto.ExpenseDto;
import com.hms.reportingservice.client.dto.HousekeepingTaskDto;
import com.hms.reportingservice.client.dto.MaintenanceIssueDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "operations-service")
public interface OperationsClient {

    @GetMapping("/api/expenses")
    List<ExpenseDto> getAllExpenses();

    @GetMapping("/api/housekeeping/tasks")
    List<HousekeepingTaskDto> getAllHousekeepingTasks();

    @GetMapping("/api/maintenance")
    List<MaintenanceIssueDto> getAllMaintenanceIssues();
}
