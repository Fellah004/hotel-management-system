package com.hms.purchaseservice.service;

import com.hms.purchaseservice.dto.request.*;
import com.hms.purchaseservice.dto.response.*;
import com.hms.purchaseservice.entity.OrderStatus;
import com.hms.purchaseservice.entity.RequestStatus;
import com.hms.purchaseservice.entity.SupplierStatus;

import java.util.List;

public interface PurchaseService {

    // --- Supplier Management ---
    SupplierResponse createSupplier(CreateSupplierRequest request);
    SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request);
    SupplierResponse getSupplierById(Long id);
    List<SupplierResponse> getAllSuppliers(SupplierStatus status);
    void deleteSupplier(Long id);

    // --- Purchase Requests ---
    PurchaseRequestResponse createPurchaseRequest(CreatePurchaseRequest request, Long requesterId, String role);
    PurchaseRequestResponse getPurchaseRequestById(Long id);
    List<PurchaseRequestResponse> getAllPurchaseRequests(RequestStatus status);
    List<PurchaseRequestResponse> getPurchaseRequestsByRequester(Long requesterId);
    PurchaseRequestResponse approvePurchaseRequest(Long id, ApprovePurchaseRequest request);
    PurchaseRequestResponse rejectPurchaseRequest(Long id, RejectPurchaseRequest request);
    PurchaseRequestResponse cancelPurchaseRequest(Long id, Long requesterId, String role);

    // --- Supplier Quotations ---
    SupplierQuotationResponse createQuotation(CreateQuotationRequest request);
    SupplierQuotationResponse getQuotationById(Long id);
    List<SupplierQuotationResponse> getQuotationsByPurchaseRequest(Long prId);
    SupplierQuotationResponse selectQuotation(Long id, Long managerId);

    // --- Purchase Orders ---
    PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request, Long staffId);
    PurchaseOrderResponse getPurchaseOrderById(Long id);
    List<PurchaseOrderResponse> getAllPurchaseOrders(OrderStatus status);
    PurchaseOrderResponse approvePurchaseOrder(Long id, ApprovePurchaseOrderRequest request);
    PurchaseOrderResponse sendPurchaseOrderToSupplier(Long id, Long staffId);
    PurchaseOrderResponse cancelPurchaseOrder(Long id, Long managerId, String remarks);

    // --- Goods Receiving (GRN) ---
    GoodsReceiptResponse recordGoodsReceipt(CreateGoodsReceiptRequest request);
    GoodsReceiptResponse getGoodsReceiptById(Long id);
    List<GoodsReceiptResponse> getGoodsReceiptsByPurchaseOrder(Long poId);

    // --- Audit Logs ---
    List<PurchaseAuditLogResponse> getAuditLogs(String entityType, Long entityId);
    List<PurchaseAuditLogResponse> getAllAuditLogs();
}
