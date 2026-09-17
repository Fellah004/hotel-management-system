package com.hms.inventoryservice.repository;

import com.hms.inventoryservice.entity.ReorderRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReorderRuleRepository extends JpaRepository<ReorderRule, Long> {
    Optional<ReorderRule> findByItemId(Long itemId);
}
