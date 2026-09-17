package com.hms.staffservice.service;

import com.hms.staffservice.dto.request.CreateStaffRequest;
import com.hms.staffservice.dto.response.StaffResponse;
import com.hms.staffservice.entity.Staff;
import com.hms.staffservice.entity.StaffRole;
import com.hms.staffservice.exception.DuplicateResourceException;
import com.hms.staffservice.repository.StaffRepository;
import com.hms.staffservice.service.impl.StaffServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private StaffServiceImpl staffService;

    private Staff sampleStaff;

    @BeforeEach
    void setUp() {
        sampleStaff = Staff.builder()
                .id(1L)
                .userId(10L)
                .employeeCode("HK001")
                .fullName("Helen Housekeeper")
                .address("123 Street")
                .nic("NIC123456")
                .salary(new BigDecimal("2200.00"))
                .age(28)
                .occupation("Housekeeper")
                .email("helen@hms.com")
                .phone("+1234567890")
                .role(StaffRole.HOUSEKEEPER)
                .active(true)
                .build();
    }

    @Test
    void createStaff_Success() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .employeeCode("HK001")
                .fullName("Helen Housekeeper")
                .nic("NIC123456")
                .salary(new BigDecimal("2200.00"))
                .age(28)
                .occupation("Housekeeper")
                .email("helen@hms.com")
                .phone("+1234567890")
                .role(StaffRole.HOUSEKEEPER)
                .build();

        when(staffRepository.existsByEmployeeCode("HK001")).thenReturn(false);
        when(staffRepository.existsByEmail("helen@hms.com")).thenReturn(false);
        when(staffRepository.existsByNic("NIC123456")).thenReturn(false);
        when(staffRepository.save(any(Staff.class))).thenAnswer(i -> {
            Staff s = i.getArgument(0);
            s.setId(1L);
            return s;
        });

        StaffResponse response = staffService.createStaff(request);

        assertNotNull(response);
        assertEquals("HK001", response.getEmployeeCode());
        assertEquals("Helen Housekeeper", response.getFullName());
    }

    @Test
    void createStaff_DuplicateEmployeeCode_ThrowsDuplicateResourceException() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .employeeCode("HK001")
                .email("helen@hms.com")
                .nic("NIC123456")
                .build();

        when(staffRepository.existsByEmployeeCode("HK001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> staffService.createStaff(request));
    }

    @Test
    void getStaffById_Owner_CanSeeSalaryAndNic() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(sampleStaff));

        StaffResponse response = staffService.getStaffById(1L, "OWNER");

        assertNotNull(response);
        assertEquals(new BigDecimal("2200.00"), response.getSalary());
        assertEquals("NIC123456", response.getNic());
    }

    @Test
    void getStaffById_Receptionist_MasksSalaryAndNic() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(sampleStaff));

        StaffResponse response = staffService.getStaffById(1L, "RECEPTIONIST");

        assertNotNull(response);
        assertNull(response.getSalary());
        assertEquals("********", response.getNic());
    }
}
