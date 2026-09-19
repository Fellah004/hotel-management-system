package com.hms.billingservice.service;

import com.hms.billingservice.dto.request.AddBillItemRequest;
import com.hms.billingservice.dto.request.CreateBillRequest;
import com.hms.billingservice.dto.request.FinalizeBillRequest;
import com.hms.billingservice.dto.response.BillItemResponse;
import com.hms.billingservice.dto.response.BillResponse;
import com.hms.billingservice.dto.response.PrintBillResponse;
import com.hms.billingservice.entity.Bill;
import com.hms.billingservice.entity.BillItem;
import com.hms.billingservice.entity.BillItemType;
import com.hms.billingservice.entity.BillStatus;
import com.hms.billingservice.event.publisher.BillingEventPublisher;
import com.hms.billingservice.exception.BusinessRuleException;
import com.hms.billingservice.repository.BillItemRepository;
import com.hms.billingservice.repository.BillRepository;
import com.hms.billingservice.service.impl.BillingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillItemRepository billItemRepository;

    @Mock
    private BillingEventPublisher billingEventPublisher;

    @Mock
    private com.hms.billingservice.client.ReservationClient reservationClient;

    @Mock
    private com.hms.billingservice.client.RoomClient roomClient;

    @InjectMocks
    private BillingServiceImpl billingService;

    private Bill sampleBill;

    @BeforeEach
    void setUp() {
        sampleBill = Bill.builder()
                .id(1L)
                .billingNumber("INV-12345678")
                .reservationId(100L)
                .guestId(50L)
                .roomId(201L)
                .checkInDate(LocalDate.of(2026, 10, 1))
                .checkOutDate(LocalDate.of(2026, 10, 5))
                .subtotal(new BigDecimal("400.00"))
                .taxAmount(new BigDecimal("40.00"))
                .discountAmount(BigDecimal.ZERO)
                .breakageCharges(BigDecimal.ZERO)
                .lateCheckoutCharges(BigDecimal.ZERO)
                .upgradeCharges(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("440.00"))
                .paidAmount(new BigDecimal("440.00"))
                .outstandingAmount(BigDecimal.ZERO)
                .status(BillStatus.PENDING)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create bill successfully")
    void testCreateBill_Success() {
        CreateBillRequest request = CreateBillRequest.builder()
                .reservationId(100L)
                .guestId(50L)
                .roomId(201L)
                .checkInDate(LocalDate.of(2026, 10, 1))
                .checkOutDate(LocalDate.of(2026, 10, 5))
                .build();

        when(billRepository.findByReservationId(100L)).thenReturn(Optional.empty());
        when(billRepository.save(any(Bill.class))).thenReturn(sampleBill);

        BillResponse response = billingService.createBill(request);

        assertNotNull(response);
        assertEquals("INV-12345678", response.getBillingNumber());
        assertEquals(100L, response.getReservationId());
        verify(billRepository, atLeastOnce()).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should add item to bill and recalculate total deterministically")
    void testAddItemToBill_Success() {
        AddBillItemRequest request = AddBillItemRequest.builder()
                .description("Minibar snacks")
                .itemType(BillItemType.SERVICE_CHARGE)
                .quantity(2)
                .unitPrice(new BigDecimal("15.00"))
                .build();

        BillItem sampleItem = BillItem.builder()
                .id(10L)
                .bill(sampleBill)
                .description("Minibar snacks")
                .itemType(BillItemType.SERVICE_CHARGE)
                .quantity(2)
                .unitPrice(new BigDecimal("15.00"))
                .totalPrice(new BigDecimal("30.00"))
                .createdAt(LocalDateTime.now())
                .build();

        when(billRepository.findById(1L)).thenReturn(Optional.of(sampleBill));
        when(billItemRepository.save(any(BillItem.class))).thenReturn(sampleItem);
        when(billRepository.save(any(Bill.class))).thenReturn(sampleBill);

        BillItemResponse response = billingService.addItemToBill(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("30.00"), response.getTotalPrice());
        verify(billItemRepository, times(1)).save(any(BillItem.class));
        verify(billRepository, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should finalize bill and publish event, locking future changes")
    void testFinalizeBill_Success() {
        FinalizeBillRequest request = FinalizeBillRequest.builder()
                .paidAmount(new BigDecimal("440.00"))
                .discountAmount(new BigDecimal("10.00"))
                .taxPercentage(new BigDecimal("10.0"))
                .build();

        when(billRepository.findById(1L)).thenReturn(Optional.of(sampleBill));
        when(billRepository.save(any(Bill.class))).thenReturn(sampleBill);

        BillResponse response = billingService.finalizeBill(1L, request);

        assertNotNull(response);
        assertEquals(BillStatus.FINALIZED, sampleBill.getStatus());
        assertNotNull(sampleBill.getFinalizedAt());
        verify(billingEventPublisher, times(1)).publishBillFinalized(any(Bill.class));
    }

    @Test
    @DisplayName("Should return structured printable bill representation")
    void testGetPrintBill_Success() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(sampleBill));

        PrintBillResponse printResponse = billingService.getPrintBill(1L);

        assertNotNull(printResponse);
        assertEquals("INV-12345678", printResponse.getBillingNumber());
        assertNotNull(printResponse.getHotelHeader());
        assertNotNull(printResponse.getFooterNotes());
    }
}
