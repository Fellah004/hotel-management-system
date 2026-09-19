package com.hms.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.paymentservice.dto.request.CreatePaymentRequest;
import com.hms.paymentservice.dto.request.RefundRequest;
import com.hms.paymentservice.dto.response.PaymentResponse;
import com.hms.paymentservice.dto.response.RefundResponse;
import com.hms.paymentservice.entity.PaymentMethod;
import com.hms.paymentservice.entity.PaymentStatus;
import com.hms.paymentservice.security.JwtTokenProvider;
import com.hms.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void testProcessPayment() throws Exception {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .reservationId(101L)
                .amount(new BigDecimal("150.00"))
                .paymentMethod(PaymentMethod.UPI)
                .idempotencyKey("KEY-1234")
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .reservationId(101L)
                .amount(new BigDecimal("150.00"))
                .paymentMethod(PaymentMethod.UPI)
                .status(PaymentStatus.SUCCESS)
                .providerReference("REF-123")
                .idempotencyKey("KEY-1234")
                .paymentTime(LocalDateTime.now())
                .build();

        when(paymentService.processPayment(any(CreatePaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testProcessRefund() throws Exception {
        RefundRequest request = RefundRequest.builder()
                .amount(new BigDecimal("50.00"))
                .reason("Customer complaint discount")
                .build();

        RefundResponse response = RefundResponse.builder()
                .id(1L)
                .paymentId(10L)
                .amount(new BigDecimal("50.00"))
                .reason("Customer complaint discount")
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.processRefund(eq(10L), any(RefundRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments/10/refund")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(50.00));
    }
}
