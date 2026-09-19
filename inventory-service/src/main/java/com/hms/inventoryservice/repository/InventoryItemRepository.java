package com.hms.inventoryservice.repository;

import com.hms.inventoryservice.entity.InventoryCategory;
import com.hms.inventoryservice.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByItemCode(String itemCode);
    boolean existsByItemCode(String itemCode);
    List<InventoryItem> findByCategory(InventoryCategory category);
    List<InventoryItem> findByActiveTrue();

    @Query("SELECT i FROM InventoryItem i WHERE i.active = true AND i.quantityInStock <= i.minimumThreshold")
    List<InventoryItem> findLowStockItems();
}
