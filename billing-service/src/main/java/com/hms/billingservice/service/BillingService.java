package com.hms.billingservice.service;

import com.hms.billingservice.dto.request.AddBillItemRequest;
import com.hms.billingservice.dto.request.CreateBillRequest;
import com.hms.billingservice.dto.request.FinalizeBillRequest;
import com.hms.billingservice.dto.response.BillItemResponse;
import com.hms.billingservice.dto.response.BillResponse;
import com.hms.billingservice.dto.response.PrintBillResponse;

public interface BillingService {
    BillResponse createBill(CreateBillRequest request);
    BillResponse getBillById(Long id);
    BillResponse getBillByReservationId(Long reservationId);
    BillItemResponse addItemToBill(Long billId, AddBillItemRequest request);
    BillResponse calculateBill(Long id);
    BillResponse finalizeBill(Long id, FinalizeBillRequest request);
    PrintBillResponse getPrintBill(Long id);
    BillResponse recordPaymentForReservation(Long reservationId, java.math.BigDecimal amount);
}
