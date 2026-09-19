package com.hms.roomservice.repository;

import com.hms.roomservice.entity.HallCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HallCategoryRepository extends JpaRepository<HallCategory, Long> {
    Optional<HallCategory> findByName(String name);
    boolean existsByName(String name);
}
