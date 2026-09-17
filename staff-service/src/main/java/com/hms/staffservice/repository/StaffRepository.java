package com.hms.staffservice.repository;

import com.hms.staffservice.entity.Staff;
import com.hms.staffservice.entity.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmployeeCode(String employeeCode);
    Optional<Staff> findByEmail(String email);
    Optional<Staff> findByNic(String nic);
    Optional<Staff> findByUserId(Long userId);
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);
    boolean existsByNic(String nic);
    List<Staff> findByRole(StaffRole role);
    List<Staff> findByRoleAndActiveTrue(StaffRole role);
    List<Staff> findByActiveTrue();
}
