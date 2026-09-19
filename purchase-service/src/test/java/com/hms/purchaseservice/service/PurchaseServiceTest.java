package com.hms.purchaseservice.service;

import com.hms.purchaseservice.client.InventoryClient;
import com.hms.purchaseservice.dto.request.*;
import com.hms.purchaseservice.dto.response.*;
import com.hms.purchaseservice.entity.*;
import com.hms.purchaseservice.event.PurchaseEventPublisher;
import com.hms.purchaseservice.exception.BusinessRuleException;
import com.hms.purchaseservice.exception.DuplicateResourceException;
import com.hms.purchaseservice.repository.*;
import com.hms.purchaseservice.service.impl.PurchaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private PurchaseRequestRepository purchaseRequestRepository;
    @Mock
    private PurchaseRequestItemRepository purchaseRequestItemRepository;
    @Mock
    private SupplierQuotationRepository quotationRepository;
    @Mock
    private SupplierQuotationItemRepository quotationItemRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;
    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;
    @Mock
    private GoodsReceiptItemRepository goodsReceiptItemRepository;
    @Mock
    private PurchaseAuditLogRepository auditLogRepository;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private PurchaseEventPublisher eventPublisher;
    private PurchaseServiceImpl purchaseService;

    private Supplier sampleSupplier;

    @BeforeEach
    void setUp() {
        eventPublisher = new PurchaseEventPublisher(rabbitTemplate);
        purchaseService = new PurchaseServiceImpl(
                supplierRepository,
                purchaseRequestRepository,
                quotationRepository,
                purchaseOrderRepository,
                goodsReceiptRepository,
                auditLogRepository,
                inventoryClient,
                eventPublisher
        );

        sampleSupplier = Supplier.builder()
                .id(1L)
                .supplierCode("SUP-001")
                .name("Grand Linen Supplies")
                .contactPerson("Alice Smith")
                .email("alice@grandlinen.com")
                .phone("1234567890")
                .paymentTerms(PaymentTerms.NET_30)
                .status(SupplierStatus.ACTIVE)
                .rating(BigDecimal.valueOf(4.8))
                .build();
    }

    @Test
    void createSupplier_Success() {
        CreateSupplierRequest request = CreateSupplierRequest.builder()
                .name("Grand Linen Supplies")
                .contactPerson("Alice Smith")
                .email("alice@grandlinen.com")
                .phone("1234567890")
                .paymentTerms(PaymentTerms.NET_30)
                .build();

        when(supplierRepository.existsByEmail("alice@grandlinen.com")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(sampleSupplier);

        SupplierResponse response = purchaseService.createSupplier(request);

        assertNotNull(response);
        assertEquals("SUP-001", response.getSupplierCode());
        assertEquals("Grand Linen Supplies", response.getName());
    }

    @Test
    void createSupplier_DuplicateEmail_ThrowsDuplicateResourceException() {
        CreateSupplierRequest request = CreateSupplierRequest.builder()
                .name("Grand Linen Supplies")
                .email("alice@grandlinen.com")
                .build();

        when(supplierRepository.existsByEmail("alice@grandlinen.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> purchaseService.createSupplier(request));
    }

    @Test
    void createPurchaseRequest_Success() {
        PurchaseRequestItemRequest itemReq = PurchaseRequestItemRequest.builder()
                .itemId(1L)
                .itemName("Bath Towels")
                .quantityRequested(50)
                .unitOfMeasure("PIECES")
                .estimatedUnitPrice(BigDecimal.valueOf(10.00))
                .build();

        CreatePurchaseRequest request = CreatePurchaseRequest.builder()
                .departmentId(1L)
                .reason("Low stock replenishment")
                .priority(PriorityLevel.HIGH)
                .requiredByDate(LocalDate.now().plusDays(5))
                .items(List.of(itemReq))
                .build();

        when(purchaseRequestRepository.save(any(PurchaseRequest.class))).thenAnswer(i -> {
            PurchaseRequest pr = i.getArgument(0);
            pr.setId(10L);
            return pr;
        });

        PurchaseRequestResponse response = purchaseService.createPurchaseRequest(request, 2L, "HOUSEKEEPER");

        assertNotNull(response);
        assertEquals(RequestStatus.SUBMITTED, response.getStatus());
        assertEquals(PriorityLevel.HIGH, response.getPriority());
        assertEquals(1, response.getItems().size());
    }

    @Test
    void approvePurchaseRequest_Success() {
        PurchaseRequest pr = PurchaseRequest.builder()
                .id(10L)
                .requestCode("PR-1001")
                .status(RequestStatus.SUBMITTED)
                .items(new ArrayList<>())
                .build();

        when(purchaseRequestRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(purchaseRequestRepository.save(any(PurchaseRequest.class))).thenAnswer(i -> i.getArgument(0));

        ApprovePurchaseRequest request = ApprovePurchaseRequest.builder()
                .managerId(5L)
                .remarks("Approved for purchase")
                .build();

        PurchaseRequestResponse response = purchaseService.approvePurchaseRequest(10L, request);

        assertNotNull(response);
        assertEquals(RequestStatus.APPROVED, response.getStatus());
        assertEquals(5L, response.getApprovedByManagerId());
    }

    @Test
    void createQuotation_Success() {
        PurchaseRequest pr = PurchaseRequest.builder()
                .id(10L)
                .status(RequestStatus.APPROVED)
                .items(new ArrayList<>())
                .build();

        QuotationItemRequest itemReq = QuotationItemRequest.builder()
                .itemId(1L)
                .itemName("Bath Towels")
                .quotedQuantity(50)
                .unitPrice(BigDecimal.valueOf(9.50))
                .discountPercentage(BigDecimal.valueOf(5))
                .taxPercentage(BigDecimal.valueOf(10))
                .build();

        CreateQuotationRequest request = CreateQuotationRequest.builder()
                .purchaseRequestId(10L)
                .supplierId(1L)
                .quotationDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(30))
                .deliveryLeadTimeDays(5)
                .items(List.of(itemReq))
                .build();

        when(purchaseRequestRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));
        when(quotationRepository.save(any(SupplierQuotation.class))).thenAnswer(i -> {
            SupplierQuotation q = i.getArgument(0);
            q.setId(100L);
            return q;
        });

        SupplierQuotationResponse response = purchaseService.createQuotation(request);

        assertNotNull(response);
        assertEquals(QuotationStatus.RECEIVED, response.getStatus());
        assertEquals("Grand Linen Supplies", response.getSupplierName());
    }

    @Test
    void createPurchaseOrder_Success() {
        PurchaseOrderItemRequest itemReq = PurchaseOrderItemRequest.builder()
                .itemId(1L)
                .itemName("Bath Towels")
                .orderedQuantity(50)
                .unitPrice(BigDecimal.valueOf(9.50))
                .taxRate(BigDecimal.valueOf(10))
                .build();

        CreatePurchaseOrderRequest request = CreatePurchaseOrderRequest.builder()
                .supplierId(1L)
                .purchaseRequestId(10L)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(7))
                .items(List.of(itemReq))
                .build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> {
            PurchaseOrder po = i.getArgument(0);
            po.setId(200L);
            return po;
        });

        PurchaseOrderResponse response = purchaseService.createPurchaseOrder(request, 1L);

        assertNotNull(response);
        assertEquals(OrderStatus.PENDING_APPROVAL, response.getStatus());
        assertEquals("Grand Linen Supplies", response.getSupplierName());
    }

    @Test
    void recordGoodsReceipt_FullDelivery_Success_UpdatesPOToReceived() {
        PurchaseOrderItem poItem = PurchaseOrderItem.builder()
                .id(501L)
                .itemId(1L)
                .itemName("Bath Towels")
                .orderedQuantity(50)
                .receivedQuantity(0)
                .unitPrice(BigDecimal.valueOf(9.50))
                .build();

        PurchaseOrder po = PurchaseOrder.builder()
                .id(200L)
                .orderCode("PO-TEST")
                .supplier(sampleSupplier)
                .status(OrderStatus.SENT_TO_SUPPLIER)
                .items(new ArrayList<>(List.of(poItem)))
                .build();

        GoodsReceiptItemRequest itemReq = GoodsReceiptItemRequest.builder()
                .purchaseOrderItemId(501L)
                .receivedQuantity(50)
                .acceptedQuantity(50)
                .rejectedQuantity(0)
                .batchNumber("BATCH-001")
                .build();

        CreateGoodsReceiptRequest request = CreateGoodsReceiptRequest.builder()
                .purchaseOrderId(200L)
                .deliveryNoteNumber("DN-999")
                .receivedDate(LocalDate.now())
                .receivedByStaffId(3L)
                .items(List.of(itemReq))
                .build();

        when(purchaseOrderRepository.findById(200L)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));
        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(i -> {
            GoodsReceipt gr = i.getArgument(0);
            gr.setId(300L);
            return gr;
        });

        GoodsReceiptResponse response = purchaseService.recordGoodsReceipt(request);

        assertNotNull(response);
        assertEquals(OrderStatus.RECEIVED, po.getStatus());
        assertEquals(50, poItem.getReceivedQuantity());
    }

    @Test
    void recordGoodsReceipt_AcceptedPlusRejectedMismatch_ThrowsBusinessRuleException() {
        PurchaseOrderItem poItem = PurchaseOrderItem.builder()
                .id(501L)
                .itemId(1L)
                .orderedQuantity(50)
                .receivedQuantity(0)
                .build();

        PurchaseOrder po = PurchaseOrder.builder()
                .id(200L)
                .status(OrderStatus.SENT_TO_SUPPLIER)
                .items(List.of(poItem))
                .build();

        GoodsReceiptItemRequest itemReq = GoodsReceiptItemRequest.builder()
                .purchaseOrderItemId(501L)
                .receivedQuantity(50)
                .acceptedQuantity(30)
                .rejectedQuantity(10) // 30 + 10 != 50
                .build();

        CreateGoodsReceiptRequest request = CreateGoodsReceiptRequest.builder()
                .purchaseOrderId(200L)
                .receivedDate(LocalDate.now())
                .receivedByStaffId(3L)
                .items(List.of(itemReq))
                .build();

        when(purchaseOrderRepository.findById(200L)).thenReturn(Optional.of(po));

        assertThrows(BusinessRuleException.class, () -> purchaseService.recordGoodsReceipt(request));
    }
}
