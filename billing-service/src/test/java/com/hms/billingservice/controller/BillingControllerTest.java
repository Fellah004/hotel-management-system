package com.hms.billingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hms.billingservice.dto.request.AddBillItemRequest;
import com.hms.billingservice.dto.request.CreateBillRequest;
import com.hms.billingservice.dto.response.BillItemResponse;
import com.hms.billingservice.dto.response.BillResponse;
import com.hms.billingservice.dto.response.PrintBillResponse;
import com.hms.billingservice.entity.BillItemType;
import com.hms.billingservice.entity.BillStatus;
import com.hms.billingservice.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BillingService billingService;

    @InjectMocks
    private BillingController billingController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(billingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCreateBill() throws Exception {
        CreateBillRequest request = CreateBillRequest.builder()
                .reservationId(101L)
                .guestId(202L)
                .roomId(303L)
                .checkInDate(LocalDate.of(2026, 10, 1))
                .checkOutDate(LocalDate.of(2026, 10, 4))
                .build();

        BillResponse response = BillResponse.builder()
                .id(1L)
                .billingNumber("INV-101")
                .reservationId(101L)
                .guestId(202L)
                .roomId(303L)
                .status(BillStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        when(billingService.createBill(any(CreateBillRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.billingNumber").value("INV-101"));
    }

    @Test
    void testAddItemToBill() throws Exception {
        AddBillItemRequest request = AddBillItemRequest.builder()
                .description("Late checkout fee")
                .itemType(BillItemType.LATE_CHECKOUT)
                .quantity(1)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        BillItemResponse response = BillItemResponse.builder()
                .id(5L)
                .description("Late checkout fee")
                .itemType(BillItemType.LATE_CHECKOUT)
                .quantity(1)
                .unitPrice(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build();

        when(billingService.addItemToBill(eq(1L), any(AddBillItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/bills/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.totalPrice").value(50.00));
    }

    @Test
    void testGetPrintBill() throws Exception {
        PrintBillResponse response = PrintBillResponse.builder()
                .billingNumber("INV-PRINT-123")
                .guestId(202L)
                .reservationId(101L)
                .totalAmount(new BigDecimal("350.00"))
                .paidAmount(new BigDecimal("350.00"))
                .outstandingAmount(BigDecimal.ZERO)
                .status("FINALIZED")
                .hotelHeader("i-TRANSFORM ONLINE HOTEL & RESORT MANAGEMENT")
                .footerNotes("Thank you")
                .items(List.of())
                .build();

        when(billingService.getPrintBill(1L)).thenReturn(response);

        mockMvc.perform(get("/api/bills/1/print"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billingNumber").value("INV-PRINT-123"))
                .andExpect(jsonPath("$.hotelHeader").value("i-TRANSFORM ONLINE HOTEL & RESORT MANAGEMENT"));
    }
}
