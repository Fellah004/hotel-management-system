package com.hms.inventoryservice;

import com.hms.inventoryservice.entity.InventoryCategory;
import com.hms.inventoryservice.entity.InventoryItem;
import com.hms.inventoryservice.entity.InventoryUnit;
import com.hms.inventoryservice.repository.InventoryItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initInventoryData(InventoryItemRepository itemRepository) {
        return args -> {
            if (itemRepository.count() == 0) {
                itemRepository.save(InventoryItem.builder()
                        .itemCode("LIN-TWL-001")
                        .name("Bath Towels (White)")
                        .category(InventoryCategory.LINEN)
                        .unit(InventoryUnit.PIECES)
                        .quantityInStock(150)
                        .minimumThreshold(30)
                        .unitCost(new BigDecimal("12.50"))
                        .description("Premium cotton white bath towels")
                        .active(true)
                        .build());

                itemRepository.save(InventoryItem.builder()
                        .itemCode("LIN-SHT-001")
                        .name("King Size Bed Sheets")
                        .category(InventoryCategory.LINEN)
                        .unit(InventoryUnit.PIECES)
                        .quantityInStock(80)
                        .minimumThreshold(20)
                        .unitCost(new BigDecimal("25.00"))
                        .description("500-thread count king bed sheets")
                        .active(true)
                        .build());

                itemRepository.save(InventoryItem.builder()
                        .itemCode("TOI-SHP-001")
                        .name("Luxury Shampoo (50ml)")
                        .category(InventoryCategory.TOILETRIES)
                        .unit(InventoryUnit.BOTTLES)
                        .quantityInStock(300)
                        .minimumThreshold(50)
                        .unitCost(new BigDecimal("1.20"))
                        .description("Organic aloe vera guest shampoo")
                        .active(true)
                        .build());

                itemRepository.save(InventoryItem.builder()
                        .itemCode("FNB-COF-001")
                        .name("Espresso Coffee Pods")
                        .category(InventoryCategory.FOOD_BEVERAGE)
                        .unit(InventoryUnit.PACKS)
                        .quantityInStock(200)
                        .minimumThreshold(40)
                        .unitCost(new BigDecimal("8.00"))
                        .description("Box of 10 espresso pods for in-room coffee machines")
                        .active(true)
                        .build());
            }
        };
    }
}
