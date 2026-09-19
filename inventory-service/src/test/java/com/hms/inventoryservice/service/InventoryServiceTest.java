package com.hms.inventoryservice.service;

import com.hms.inventoryservice.dto.request.CreateItemRequest;
import com.hms.inventoryservice.dto.request.StockMovementRequest;
import com.hms.inventoryservice.dto.response.ItemResponse;
import com.hms.inventoryservice.dto.response.StockMovementResponse;
import com.hms.inventoryservice.entity.InventoryCategory;
import com.hms.inventoryservice.entity.InventoryItem;
import com.hms.inventoryservice.entity.InventoryUnit;
import com.hms.inventoryservice.entity.MovementType;
import com.hms.inventoryservice.event.publisher.InventoryEventPublisher;
import com.hms.inventoryservice.exception.BusinessRuleException;
import com.hms.inventoryservice.exception.DuplicateResourceException;
import com.hms.inventoryservice.repository.InventoryItemRepository;
import com.hms.inventoryservice.repository.ReorderRuleRepository;
import com.hms.inventoryservice.repository.StockMovementRepository;
import com.hms.inventoryservice.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository itemRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private ReorderRuleRepository reorderRuleRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private InventoryItem sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = InventoryItem.builder()
                .id(1L)
                .itemCode("LIN-TWL-001")
                .name("Bath Towels")
                .category(InventoryCategory.LINEN)
                .unit(InventoryUnit.PIECES)
                .quantityInStock(50)
                .minimumThreshold(20)
                .unitCost(new BigDecimal("10.00"))
                .active(true)
                .build();
    }

    @Test
    void createItem_Success() {
        CreateItemRequest request = CreateItemRequest.builder()
                .itemCode("LIN-TWL-001")
                .name("Bath Towels")
                .category(InventoryCategory.LINEN)
                .initialQuantity(50)
                .minimumThreshold(20)
                .build();

        when(itemRepository.existsByItemCode("LIN-TWL-001")).thenReturn(false);
        when(itemRepository.save(any(InventoryItem.class))).thenAnswer(i -> {
            InventoryItem item = i.getArgument(0);
            item.setId(1L);
            return item;
        });

        ItemResponse response = inventoryService.createItem(request);

        assertNotNull(response);
        assertEquals("LIN-TWL-001", response.getItemCode());
        assertEquals(50, response.getQuantityInStock());
        verify(movementRepository).save(any());
    }

    @Test
    void createItem_DuplicateItemCode_ThrowsDuplicateResourceException() {
        CreateItemRequest request = CreateItemRequest.builder()
                .itemCode("LIN-TWL-001")
                .name("Bath Towels")
                .build();

        when(itemRepository.existsByItemCode("LIN-TWL-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> inventoryService.createItem(request));
    }

    @Test
    void stockOut_Success_TriggersLowStockWhenBelowThreshold() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(movementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Consume 35 towels -> 50 - 35 = 15 remaining (which is <= threshold 20)
        StockMovementRequest request = StockMovementRequest.builder()
                .quantity(35)
                .reason("Housekeeping replenishment")
                .build();

        StockMovementResponse response = inventoryService.stockOut(1L, request);

        assertNotNull(response);
        assertEquals(15, response.getBalanceAfter());
        assertEquals(MovementType.STOCK_OUT, response.getMovementType());
        verify(eventPublisher).publishLowStock(eq(1L), eq("LIN-TWL-001"), eq("Bath Towels"), eq(15), eq(20));
    }

    @Test
    void stockOut_InsufficientQuantity_ThrowsBusinessRuleException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        // Attempting to consume 60 when only 50 available
        StockMovementRequest request = StockMovementRequest.builder()
                .quantity(60)
                .reason("Exceeds stock")
                .build();

        assertThrows(BusinessRuleException.class, () -> inventoryService.stockOut(1L, request));
    }
}
