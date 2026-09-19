package com.hms.purchaseservice.client;

import com.hms.purchaseservice.config.FeignConfig;
import com.hms.purchaseservice.dto.response.ItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class, configuration = FeignConfig.class)
public interface InventoryClient {

    @GetMapping("/api/inventory/{id}")
    ItemResponse getItemById(@PathVariable("id") Long id);

    @GetMapping("/api/inventory/code/{itemCode}")
    ItemResponse getItemByCode(@PathVariable("itemCode") String itemCode);
}
