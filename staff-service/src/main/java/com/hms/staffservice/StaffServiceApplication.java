package com.hms.staffservice;

import com.hms.staffservice.entity.Shift;
import com.hms.staffservice.entity.ShiftType;
import com.hms.staffservice.entity.Staff;
import com.hms.staffservice.entity.StaffRole;
import com.hms.staffservice.repository.ShiftRepository;
import com.hms.staffservice.repository.StaffRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Set;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class StaffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StaffServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initStaffData(StaffRepository staffRepository, ShiftRepository shiftRepository) {
        return args -> {
            if (staffRepository.count() == 0) {
                Staff manager = staffRepository.save(Staff.builder()
                        .userId(3L)
                        .employeeCode("EMP-M001")
                        .fullName("Michael Manager")
                        .address("100 Hotel Blvd")
                        .nic("NIC1980001")
                        .salary(new BigDecimal("5000.00"))
                        .age(42)
                        .occupation("Hotel Operations Manager")
                        .email("manager@hms.com")
                        .phone("+1234567891")
                        .role(StaffRole.MANAGER)
                        .active(true)
                        .build());

                Staff receptionist = staffRepository.save(Staff.builder()
                        .userId(4L)
                        .employeeCode("EMP-R001")
                        .fullName("Rachel Receptionist")
                        .address("101 Front Desk Way")
                        .nic("NIC1992002")
                        .salary(new BigDecimal("3000.00"))
                        .age(30)
                        .occupation("Front Desk Officer")
                        .email("receptionist@hms.com")
                        .phone("+1234567892")
                        .role(StaffRole.RECEPTIONIST)
                        .active(true)
                        .build());

                Staff housekeeper1 = staffRepository.save(Staff.builder()
                        .userId(5L)
                        .employeeCode("HK001")
                        .fullName("Helen Housekeeper")
                        .address("102 Clean Ave")
                        .nic("NIC1995003")
                        .salary(new BigDecimal("2200.00"))
                        .age(28)
                        .occupation("Senior Housekeeper")
                        .email("housekeeper@hms.com")
                        .phone("+1234567893")
                        .role(StaffRole.HOUSEKEEPER)
                        .active(true)
                        .build());

                Staff housekeeper2 = staffRepository.save(Staff.builder()
                        .userId(null)
                        .employeeCode("HK002")
                        .fullName("Harry Housekeeper")
                        .address("103 Clean Ave")
                        .nic("NIC1996004")
                        .salary(new BigDecimal("2100.00"))
                        .age(26)
                        .occupation("Housekeeper")
                        .email("harry.hk@hms.com")
                        .phone("+1234567894")
                        .role(StaffRole.HOUSEKEEPER)
                        .active(true)
                        .build());

                // Seed Shifts
                shiftRepository.save(Shift.builder()
                        .name(ShiftType.MORNING)
                        .startTime(LocalTime.of(7, 0))
                        .endTime(LocalTime.of(15, 30))
                        .assignedStaffIds(Set.of(receptionist.getId(), housekeeper1.getId()))
                        .active(true)
                        .build());

                shiftRepository.save(Shift.builder()
                        .name(ShiftType.EVENING)
                        .startTime(LocalTime.of(15, 0))
                        .endTime(LocalTime.of(23, 30))
                        .assignedStaffIds(Set.of(housekeeper2.getId()))
                        .active(true)
                        .build());

                shiftRepository.save(Shift.builder()
                        .name(ShiftType.NIGHT)
                        .startTime(LocalTime.of(23, 0))
                        .endTime(LocalTime.of(7, 30))
                        .assignedStaffIds(Set.of())
                        .active(true)
                        .build());
            }
        };
    }
}
