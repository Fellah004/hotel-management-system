package com.hms.staffservice.service;

import com.hms.staffservice.dto.request.AttendanceRequest;
import com.hms.staffservice.dto.response.AttendanceResponse;
import com.hms.staffservice.entity.Attendance;
import com.hms.staffservice.entity.AttendanceStatus;
import com.hms.staffservice.entity.Staff;
import com.hms.staffservice.entity.StaffRole;
import com.hms.staffservice.exception.BusinessRuleException;
import com.hms.staffservice.repository.AttendanceRepository;
import com.hms.staffservice.repository.StaffRepository;
import com.hms.staffservice.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Staff activeStaff;
    private Staff inactiveStaff;

    @BeforeEach
    void setUp() {
        activeStaff = Staff.builder()
                .id(1L)
                .userId(100L)
                .employeeCode("STF-001")
                .fullName("John Doe")
                .role(StaffRole.RECEPTIONIST)
                .active(true)
                .build();

        inactiveStaff = Staff.builder()
                .id(2L)
                .userId(200L)
                .employeeCode("STF-002")
                .fullName("Jane Inactive")
                .role(StaffRole.HOUSEKEEPER)
                .active(false)
                .build();
    }

    @Test
    void checkIn_Success() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(activeStaff));
        when(attendanceRepository.findByStaffIdAndDate(1L, LocalDate.now())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> {
            Attendance a = i.getArgument(0);
            a.setId(10L);
            return a;
        });

        AttendanceResponse response = attendanceService.checkIn(1L, 5L, 100L, "RECEPTIONIST");

        assertNotNull(response);
        assertEquals(AttendanceStatus.PRESENT, response.getStatus());
        assertNotNull(response.getCheckInTime());
        assertEquals(5L, response.getShiftId());
    }

    @Test
    void checkIn_InactiveStaff_ThrowsBusinessRuleException() {
        when(staffRepository.findById(2L)).thenReturn(Optional.of(inactiveStaff));

        assertThrows(BusinessRuleException.class, () -> attendanceService.checkIn(2L, 5L, 200L, "HOUSEKEEPER"));
    }

    @Test
    void checkIn_DifferentUser_AccessDenied() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(activeStaff));

        assertThrows(BusinessRuleException.class, () -> attendanceService.checkIn(1L, 5L, 999L, "RECEPTIONIST"));
    }

    @Test
    void checkOut_Success_CalculatesWorkHours() {
        Attendance openAttendance = Attendance.builder()
                .id(10L)
                .staffId(1L)
                .date(LocalDate.now())
                .checkInTime(LocalTime.of(9, 0))
                .status(AttendanceStatus.PRESENT)
                .build();

        when(staffRepository.findById(1L)).thenReturn(Optional.of(activeStaff));
        when(attendanceRepository.findByStaffIdAndDate(1L, LocalDate.now())).thenReturn(Optional.of(openAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        AttendanceResponse response = attendanceService.checkOut(1L, 100L, "RECEPTIONIST");

        assertNotNull(response);
        assertNotNull(response.getCheckOutTime());
        assertNotNull(response.getWorkHours());
    }

    @Test
    void checkOut_CrossMidnightNightShift_Success() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Attendance nightShiftAttendance = Attendance.builder()
                .id(10L)
                .staffId(1L)
                .date(yesterday)
                .checkInTime(LocalTime.of(22, 0)) // 10:00 PM yesterday
                .status(AttendanceStatus.PRESENT)
                .build();

        when(staffRepository.findById(1L)).thenReturn(Optional.of(activeStaff));
        when(attendanceRepository.findByStaffIdAndDate(1L, LocalDate.now())).thenReturn(Optional.empty());
        when(attendanceRepository.findByStaffIdAndDate(1L, yesterday)).thenReturn(Optional.of(nightShiftAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        AttendanceResponse response = attendanceService.checkOut(1L, 100L, "RECEPTIONIST");

        assertNotNull(response);
        assertNotNull(response.getCheckOutTime());
        assertNotNull(response.getWorkHours());
    }

    @Test
    void recordAttendance_CheckOutBeforeCheckIn_ThrowsBusinessRuleException() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(activeStaff));

        AttendanceRequest request = AttendanceRequest.builder()
                .staffId(1L)
                .date(LocalDate.now())
                .checkInTime(LocalTime.of(17, 0))
                .checkOutTime(LocalTime.of(9, 0)) // Before check in!
                .build();

        assertThrows(BusinessRuleException.class, () -> attendanceService.recordAttendance(request));
    }
}
