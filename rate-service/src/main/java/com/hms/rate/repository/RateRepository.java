package com.hms.rateservice.repository;

import com.hms.rateservice.entity.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RateRepository extends JpaRepository<Rate, Long> {
    Optional<Rate> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Rate> findByCategoryId(Long categoryId);
    List<Rate> findByActiveTrue();
}
