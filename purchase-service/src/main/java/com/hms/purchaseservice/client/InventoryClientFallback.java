package com.hms.purchaseservice.client;

import com.hms.purchaseservice.dto.response.ItemResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryClientFallback implements InventoryClient {

    @Override
    public ItemResponse getItemById(Long id) {
        log.warn("Inventory fallback triggered: Failed to fetch item by id: {}", id);
        return null;
    }

    @Override
    public ItemResponse getItemByCode(String itemCode) {
        log.warn("Inventory fallback triggered: Failed to fetch item by code: {}", itemCode);
        return null;
    }
}
