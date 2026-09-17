package com.hms.inventoryservice.service.impl;

import com.hms.inventoryservice.dto.request.CreateItemRequest;
import com.hms.inventoryservice.dto.request.ReorderRuleRequest;
import com.hms.inventoryservice.dto.request.StockMovementRequest;
import com.hms.inventoryservice.dto.request.UpdateItemRequest;
import com.hms.inventoryservice.dto.response.ItemResponse;
import com.hms.inventoryservice.dto.response.ReorderRuleResponse;
import com.hms.inventoryservice.dto.response.StockMovementResponse;
import com.hms.inventoryservice.entity.*;
import com.hms.inventoryservice.event.publisher.InventoryEventPublisher;
import com.hms.inventoryservice.exception.BusinessRuleException;
import com.hms.inventoryservice.exception.DuplicateResourceException;
import com.hms.inventoryservice.exception.ResourceNotFoundException;
import com.hms.inventoryservice.repository.InventoryItemRepository;
import com.hms.inventoryservice.repository.ReorderRuleRepository;
import com.hms.inventoryservice.repository.StockMovementRepository;
import com.hms.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository itemRepository;
    private final StockMovementRepository movementRepository;
    private final ReorderRuleRepository reorderRuleRepository;
    private final InventoryEventPublisher eventPublisher;

    @Override
    @Transactional
    public ItemResponse createItem(CreateItemRequest request) {
        if (itemRepository.existsByItemCode(request.getItemCode())) {
            throw new DuplicateResourceException("Item code already exists: " + request.getItemCode());
        }

        InventoryItem item = InventoryItem.builder()
                .itemCode(request.getItemCode())
                .name(request.getName())
                .category(request.getCategory())
                .unit(request.getUnit() != null ? request.getUnit() : InventoryUnit.PIECES)
                .quantityInStock(request.getInitialQuantity() != null ? request.getInitialQuantity() : 0)
                .minimumThreshold(request.getMinimumThreshold() != null ? request.getMinimumThreshold() : 10)
                .unitCost(request.getUnitCost())
                .description(request.getDescription())
                .active(true)
                .build();

        InventoryItem saved = itemRepository.save(item);

        // Record initial stock movement if quantity > 0
        if (saved.getQuantityInStock() > 0) {
            StockMovement movement = StockMovement.builder()
                    .itemId(saved.getId())
                    .movementType(MovementType.STOCK_IN)
                    .quantity(saved.getQuantityInStock())
                    .balanceAfter(saved.getQuantityInStock())
                    .reason("Initial stock inventory registration")
                    .build();
            movementRepository.save(movement);
        }

        log.info("Created inventory item: {} ({})", saved.getName(), saved.getItemCode());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));
        return mapToResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemByCode(String itemCode) {
        InventoryItem item = itemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with code: " + itemCode));
        return mapToResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByCategory(InventoryCategory category) {
        return itemRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getLowStockItems() {
        return itemRepository.findLowStockItems().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long id, UpdateItemRequest request) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));

        if (request.getName() != null) item.setName(request.getName());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getUnit() != null) item.setUnit(request.getUnit());
        if (request.getMinimumThreshold() != null) item.setMinimumThreshold(request.getMinimumThreshold());
        if (request.getUnitCost() != null) item.setUnitCost(request.getUnitCost());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getActive() != null) item.setActive(request.getActive());

        InventoryItem updated = itemRepository.save(item);
        log.info("Updated inventory item id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventory item not found with id: " + id);
        }
        itemRepository.deleteById(id);
        log.info("Deleted inventory item id: {}", id);
    }

    @Override
    @Transactional
    public StockMovementResponse stockIn(Long itemId, StockMovementRequest request) {
        InventoryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + itemId));

        int newBalance = item.getQuantityInStock() + request.getQuantity();
        item.setQuantityInStock(newBalance);
        itemRepository.save(item);

        StockMovement movement = StockMovement.builder()
                .itemId(itemId)
                .movementType(MovementType.STOCK_IN)
                .quantity(request.getQuantity())
                .balanceAfter(newBalance)
                .reason(request.getReason() != null ? request.getReason() : "Stock replenishment")
                .referenceCode(request.getReferenceCode())
                .staffId(request.getStaffId())
                .build();

        StockMovement savedMovement = movementRepository.save(movement);
        log.info("Stock in: item {} +{}, new balance={}", item.getName(), request.getQuantity(), newBalance);
        return mapToMovementResponse(savedMovement);
    }

    @Override
    @Transactional
    public StockMovementResponse stockOut(Long itemId, StockMovementRequest request) {
        InventoryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + itemId));

        if (item.getQuantityInStock() < request.getQuantity()) {
            throw new BusinessRuleException("Insufficient stock for item: " + item.getName()
                    + " (Available: " + item.getQuantityInStock() + ", Requested: " + request.getQuantity() + ")");
        }

        int newBalance = item.getQuantityInStock() - request.getQuantity();
        item.setQuantityInStock(newBalance);
        itemRepository.save(item);

        StockMovement movement = StockMovement.builder()
                .itemId(itemId)
                .movementType(MovementType.STOCK_OUT)
                .quantity(request.getQuantity())
                .balanceAfter(newBalance)
                .reason(request.getReason() != null ? request.getReason() : "Stock consumption / usage")
                .referenceCode(request.getReferenceCode())
                .staffId(request.getStaffId())
                .build();

        StockMovement savedMovement = movementRepository.save(movement);
        log.info("Stock out: item {} -{}, new balance={}", item.getName(), request.getQuantity(), newBalance);

        // Check if stock is at or below threshold
        if (newBalance <= item.getMinimumThreshold()) {
            eventPublisher.publishLowStock(item.getId(), item.getItemCode(), item.getName(), newBalance, item.getMinimumThreshold());
        }

        return mapToMovementResponse(savedMovement);
    }

    @Override
    @Transactional
    public StockMovementResponse adjustStock(Long itemId, StockMovementRequest request) {
        InventoryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + itemId));

        int newBalance = request.getQuantity();
        item.setQuantityInStock(newBalance);
        itemRepository.save(item);

        StockMovement movement = StockMovement.builder()
                .itemId(itemId)
                .movementType(MovementType.ADJUSTMENT)
                .quantity(request.getQuantity())
                .balanceAfter(newBalance)
                .reason(request.getReason() != null ? request.getReason() : "Inventory audit adjustment")
                .referenceCode(request.getReferenceCode())
                .staffId(request.getStaffId())
                .build();

        StockMovement savedMovement = movementRepository.save(movement);
        log.info("Adjusted stock for item {} to balance {}", item.getName(), newBalance);

        if (newBalance <= item.getMinimumThreshold()) {
            eventPublisher.publishLowStock(item.getId(), item.getItemCode(), item.getName(), newBalance, item.getMinimumThreshold());
        }

        return mapToMovementResponse(savedMovement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByItem(Long itemId) {
        return movementRepository.findByItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(this::mapToMovementResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReorderRuleResponse setReorderRule(ReorderRuleRequest request) {
        if (!itemRepository.existsById(request.getItemId())) {
            throw new ResourceNotFoundException("Item not found with id: " + request.getItemId());
        }

        ReorderRule rule = reorderRuleRepository.findByItemId(request.getItemId())
                .orElse(ReorderRule.builder().itemId(request.getItemId()).build());

        rule.setReorderQuantity(request.getReorderQuantity());
        rule.setPreferredSupplier(request.getPreferredSupplier());
        if (request.getAutoAlertEnabled() != null) rule.setAutoAlertEnabled(request.getAutoAlertEnabled());

        ReorderRule saved = reorderRuleRepository.save(rule);
        log.info("Configured reorder rule for item id: {}", request.getItemId());
        return mapToReorderResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReorderRuleResponse getReorderRuleByItem(Long itemId) {
        ReorderRule rule = reorderRuleRepository.findByItemId(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("No reorder rule found for item id: " + itemId));
        return mapToReorderResponse(rule);
    }

    private ItemResponse mapToResponse(InventoryItem i) {
        return ItemResponse.builder()
                .id(i.getId())
                .itemCode(i.getItemCode())
                .name(i.getName())
                .category(i.getCategory())
                .unit(i.getUnit())
                .quantityInStock(i.getQuantityInStock())
                .minimumThreshold(i.getMinimumThreshold())
                .unitCost(i.getUnitCost())
                .description(i.getDescription())
                .lowStock(i.getQuantityInStock() <= i.getMinimumThreshold())
                .active(i.isActive())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }

    private StockMovementResponse mapToMovementResponse(StockMovement m) {
        return StockMovementResponse.builder()
                .id(m.getId())
                .itemId(m.getItemId())
                .movementType(m.getMovementType())
                .quantity(m.getQuantity())
                .balanceAfter(m.getBalanceAfter())
                .reason(m.getReason())
                .referenceCode(m.getReferenceCode())
                .staffId(m.getStaffId())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private ReorderRuleResponse mapToReorderResponse(ReorderRule r) {
        return ReorderRuleResponse.builder()
                .id(r.getId())
                .itemId(r.getItemId())
                .reorderQuantity(r.getReorderQuantity())
                .preferredSupplier(r.getPreferredSupplier())
                .autoAlertEnabled(r.isAutoAlertEnabled())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
