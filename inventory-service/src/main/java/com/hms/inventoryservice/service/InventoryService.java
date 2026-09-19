package com.hms.inventoryservice.service;

import com.hms.inventoryservice.dto.request.CreateItemRequest;
import com.hms.inventoryservice.dto.request.ReorderRuleRequest;
import com.hms.inventoryservice.dto.request.StockMovementRequest;
import com.hms.inventoryservice.dto.request.UpdateItemRequest;
import com.hms.inventoryservice.dto.response.ItemResponse;
import com.hms.inventoryservice.dto.response.ReorderRuleResponse;
import com.hms.inventoryservice.dto.response.StockMovementResponse;
import com.hms.inventoryservice.entity.InventoryCategory;

import java.util.List;

public interface InventoryService {
    ItemResponse createItem(CreateItemRequest request);
    ItemResponse getItemById(Long id);
    ItemResponse getItemByCode(String itemCode);
    List<ItemResponse> getAllItems();
    List<ItemResponse> getItemsByCategory(InventoryCategory category);
    List<ItemResponse> getLowStockItems();
    ItemResponse updateItem(Long id, UpdateItemRequest request);
    void deleteItem(Long id);

    StockMovementResponse stockIn(Long itemId, StockMovementRequest request);
    StockMovementResponse stockOut(Long itemId, StockMovementRequest request);
    StockMovementResponse adjustStock(Long itemId, StockMovementRequest request);
    List<StockMovementResponse> getMovementsByItem(Long itemId);

    ReorderRuleResponse setReorderRule(ReorderRuleRequest request);
    ReorderRuleResponse getReorderRuleByItem(Long itemId);
}
