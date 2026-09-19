package com.hms.guestservice.repository;

import com.hms.guestservice.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    Optional<Guest> findByMemberCode(String memberCode);
    Optional<Guest> findByEmail(String email);
    Optional<Guest> findByUserId(Long userId);
    Optional<Guest> findByPhone(String phone);
    boolean existsByMemberCode(String memberCode);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
