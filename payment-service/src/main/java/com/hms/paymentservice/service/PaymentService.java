package com.hms.paymentservice.service;

import com.hms.paymentservice.dto.request.CreatePaymentRequest;
import com.hms.paymentservice.dto.request.RefundRequest;
import com.hms.paymentservice.dto.response.PaymentResponse;
import com.hms.paymentservice.dto.response.RefundResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(CreatePaymentRequest request);
    PaymentResponse getPaymentById(Long id);
    List<PaymentResponse> getPaymentsByReservationId(Long reservationId);
    RefundResponse processRefund(Long paymentId, RefundRequest request);
}
