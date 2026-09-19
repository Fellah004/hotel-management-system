package com.hms.purchaseservice.service.impl;

import com.hms.purchaseservice.client.InventoryClient;
import com.hms.purchaseservice.dto.request.*;
import com.hms.purchaseservice.dto.response.*;
import com.hms.purchaseservice.entity.*;
import com.hms.purchaseservice.event.PurchaseEventPublisher;
import com.hms.purchaseservice.exception.BusinessRuleException;
import com.hms.purchaseservice.exception.DuplicateResourceException;
import com.hms.purchaseservice.exception.ForbiddenException;
import com.hms.purchaseservice.exception.ResourceNotFoundException;
import com.hms.purchaseservice.repository.*;
import com.hms.purchaseservice.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

    private final SupplierRepository supplierRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final SupplierQuotationRepository quotationRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseAuditLogRepository auditLogRepository;
    private final InventoryClient inventoryClient;
    private final PurchaseEventPublisher eventPublisher;

    // --- Supplier Management ---

    @Override
    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        log.info("Registering new supplier: {}", request.getName());

        if (supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Supplier with email " + request.getEmail() + " already exists");
        }

        String supplierCode = "SUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Supplier supplier = Supplier.builder()
                .supplierCode(supplierCode)
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .taxNumber(request.getTaxNumber())
                .paymentTerms(request.getPaymentTerms() != null ? request.getPaymentTerms() : PaymentTerms.NET_30)
                .status(SupplierStatus.ACTIVE)
                .rating(request.getRating() != null ? request.getRating() : new BigDecimal("5.00"))
                .build();

        Supplier saved = supplierRepository.save(supplier);
        recordAudit("SUPPLIER", saved.getId(), "CREATED", null, "ACTIVE", "SYSTEM", "Supplier registered");

        return mapToSupplierResponse(saved);
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setTaxNumber(request.getTaxNumber());
        if (request.getPaymentTerms() != null) supplier.setPaymentTerms(request.getPaymentTerms());
        if (request.getStatus() != null) supplier.setStatus(request.getStatus());
        if (request.getRating() != null) supplier.setRating(request.getRating());

        Supplier saved = supplierRepository.save(supplier);
        recordAudit("SUPPLIER", saved.getId(), "UPDATED", null, saved.getStatus().name(), "SYSTEM", "Supplier updated");

        return mapToSupplierResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return mapToSupplierResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers(SupplierStatus status) {
        if (status != null) {
            return supplierRepository.findByStatus(status).stream()
                    .map(this::mapToSupplierResponse)
                    .collect(Collectors.toList());
        }
        return supplierRepository.findAll().stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        supplierRepository.delete(supplier);
        recordAudit("SUPPLIER", id, "DELETED", supplier.getStatus().name(), "DELETED", "SYSTEM", "Supplier deleted");
    }

    // --- Purchase Requests ---

    @Override
    @Transactional
    public PurchaseRequestResponse createPurchaseRequest(CreatePurchaseRequest request, Long requesterId, String role) {
        log.info("Creating purchase request by user: {}, role: {}", requesterId, role);

        String requestCode = "PR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PurchaseRequest pr = PurchaseRequest.builder()
                .requestCode(requestCode)
                .departmentId(request.getDepartmentId())
                .requesterId(requesterId)
                .requesterRole(role)
                .reason(request.getReason())
                .priority(request.getPriority() != null ? request.getPriority() : PriorityLevel.NORMAL)
                .status(RequestStatus.SUBMITTED)
                .requiredByDate(request.getRequiredByDate() != null ? request.getRequiredByDate() : LocalDate.now().plusWeeks(1))
                .items(new ArrayList<>())
                .build();

        for (PurchaseRequestItemRequest itemReq : request.getItems()) {
            validateInventoryItem(itemReq.getItemId());

            PurchaseRequestItem item = PurchaseRequestItem.builder()
                    .purchaseRequest(pr)
                    .itemId(itemReq.getItemId())
                    .itemName(itemReq.getItemName())
                    .quantityRequested(itemReq.getQuantityRequested())
                    .unitOfMeasure(itemReq.getUnitOfMeasure() != null ? itemReq.getUnitOfMeasure() : "PIECES")
                    .estimatedUnitPrice(itemReq.getEstimatedUnitPrice())
                    .build();
            pr.addItem(item);
        }

        PurchaseRequest saved = purchaseRequestRepository.save(pr);
        recordAudit("PURCHASE_REQUEST", saved.getId(), "CREATED", null, "SUBMITTED", role + "_" + requesterId, "PR created");

        return mapToPurchaseRequestResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseRequestResponse getPurchaseRequestById(Long id) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));
        return mapToPurchaseRequestResponse(pr);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseRequestResponse> getAllPurchaseRequests(RequestStatus status) {
        if (status != null) {
            return purchaseRequestRepository.findByStatus(status).stream()
                    .map(this::mapToPurchaseRequestResponse)
                    .collect(Collectors.toList());
        }
        return purchaseRequestRepository.findAll().stream()
                .map(this::mapToPurchaseRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseRequestResponse> getPurchaseRequestsByRequester(Long requesterId) {
        return purchaseRequestRepository.findByRequesterId(requesterId).stream()
                .map(this::mapToPurchaseRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseRequestResponse approvePurchaseRequest(Long id, ApprovePurchaseRequest request) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

        if (pr.getStatus() != RequestStatus.SUBMITTED && pr.getStatus() != RequestStatus.DRAFT) {
            throw new BusinessRuleException("Cannot approve purchase request with status: " + pr.getStatus());
        }

        pr.setStatus(RequestStatus.APPROVED);
        pr.setApprovedByManagerId(request.getManagerId());
        pr.setApprovedAt(LocalDateTime.now());

        PurchaseRequest saved = purchaseRequestRepository.save(pr);
        recordAudit("PURCHASE_REQUEST", saved.getId(), "APPROVED", "SUBMITTED", "APPROVED", "MANAGER_" + request.getManagerId(), request.getRemarks());

        return mapToPurchaseRequestResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseRequestResponse rejectPurchaseRequest(Long id, RejectPurchaseRequest request) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

        if (pr.getStatus() != RequestStatus.SUBMITTED && pr.getStatus() != RequestStatus.DRAFT) {
            throw new BusinessRuleException("Cannot reject purchase request with status: " + pr.getStatus());
        }

        pr.setStatus(RequestStatus.REJECTED);
        pr.setApprovedByManagerId(request.getManagerId());
        pr.setRejectionReason(request.getRejectionReason());

        PurchaseRequest saved = purchaseRequestRepository.save(pr);
        recordAudit("PURCHASE_REQUEST", saved.getId(), "REJECTED", "SUBMITTED", "REJECTED", "MANAGER_" + request.getManagerId(), request.getRejectionReason());

        return mapToPurchaseRequestResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseRequestResponse cancelPurchaseRequest(Long id, Long requesterId, String role) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

        if (pr.getStatus() == RequestStatus.CONVERTED_TO_ORDER || pr.getStatus() == RequestStatus.REJECTED) {
            throw new BusinessRuleException("Cannot cancel purchase request with status: " + pr.getStatus());
        }

        if (!"ADMIN".equals(role) && !"OWNER".equals(role) && !"MANAGER".equals(role) && !pr.getRequesterId().equals(requesterId)) {
            throw new ForbiddenException("You can only cancel your own purchase requests");
        }

        RequestStatus oldStatus = pr.getStatus();
        pr.setStatus(RequestStatus.CANCELLED);
        PurchaseRequest saved = purchaseRequestRepository.save(pr);

        recordAudit("PURCHASE_REQUEST", saved.getId(), "CANCELLED", oldStatus.name(), "CANCELLED", role + "_" + requesterId, "Cancelled by user");
        return mapToPurchaseRequestResponse(saved);
    }

    // --- Supplier Quotations ---

    @Override
    @Transactional
    public SupplierQuotationResponse createQuotation(CreateQuotationRequest request) {
        log.info("Recording supplier quotation for PR ID: {}, Supplier ID: {}", request.getPurchaseRequestId(), request.getSupplierId());

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        if (supplier.getStatus() == SupplierStatus.BLACKLISTED) {
            throw new BusinessRuleException("Cannot accept quotation from blacklisted supplier: " + supplier.getName());
        }
        if (supplier.getStatus() == SupplierStatus.INACTIVE) {
            throw new BusinessRuleException("Cannot accept quotation from inactive supplier: " + supplier.getName());
        }

        PurchaseRequest pr = purchaseRequestRepository.findById(request.getPurchaseRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + request.getPurchaseRequestId()));

        if (pr.getStatus() != RequestStatus.APPROVED && pr.getStatus() != RequestStatus.SUBMITTED) {
            throw new BusinessRuleException("Cannot record quotation for unapproved PR with status: " + pr.getStatus());
        }

        String quoteCode = "QT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        SupplierQuotation quotation = SupplierQuotation.builder()
                .quoteCode(quoteCode)
                .purchaseRequestId(pr.getId())
                .supplier(supplier)
                .quotationDate(request.getQuotationDate())
                .expiryDate(request.getExpiryDate())
                .paymentTerms(request.getPaymentTerms() != null ? request.getPaymentTerms() : supplier.getPaymentTerms())
                .deliveryLeadTimeDays(request.getDeliveryLeadTimeDays() != null ? request.getDeliveryLeadTimeDays() : 7)
                .status(QuotationStatus.RECEIVED)
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (QuotationItemRequest itemReq : request.getItems()) {
            BigDecimal qty = new BigDecimal(itemReq.getQuotedQuantity());
            BigDecimal itemSubtotal = itemReq.getUnitPrice().multiply(qty);

            BigDecimal discountPct = itemReq.getDiscountPercentage() != null ? itemReq.getDiscountPercentage() : BigDecimal.ZERO;
            BigDecimal discountAmt = itemSubtotal.multiply(discountPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            BigDecimal taxable = itemSubtotal.subtract(discountAmt);
            BigDecimal taxPct = itemReq.getTaxPercentage() != null ? itemReq.getTaxPercentage() : BigDecimal.ZERO;
            BigDecimal taxAmt = taxable.multiply(taxPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            BigDecimal itemTotal = taxable.add(taxAmt);

            subtotal = subtotal.add(itemSubtotal);
            totalDiscount = totalDiscount.add(discountAmt);
            totalTax = totalTax.add(taxAmt);

            SupplierQuotationItem quoteItem = SupplierQuotationItem.builder()
                    .quotation(quotation)
                    .itemId(itemReq.getItemId())
                    .itemName(itemReq.getItemName())
                    .quotedQuantity(itemReq.getQuotedQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .discountPercentage(discountPct)
                    .taxPercentage(taxPct)
                    .totalAmount(itemTotal)
                    .build();

            quotation.addItem(quoteItem);
        }

        quotation.setSubtotal(subtotal);
        quotation.setDiscountAmount(totalDiscount);
        quotation.setTaxAmount(totalTax);
        quotation.setGrandTotal(subtotal.subtract(totalDiscount).add(totalTax));

        SupplierQuotation saved = quotationRepository.save(quotation);
        recordAudit("QUOTATION", saved.getId(), "CREATED", null, "RECEIVED", "SYSTEM", "Quotation recorded for supplier: " + supplier.getName());

        return mapToQuotationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierQuotationResponse getQuotationById(Long id) {
        SupplierQuotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));
        return mapToQuotationResponse(quotation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierQuotationResponse> getQuotationsByPurchaseRequest(Long prId) {
        return quotationRepository.findByPurchaseRequestId(prId).stream()
                .map(this::mapToQuotationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupplierQuotationResponse selectQuotation(Long id, Long managerId) {
        SupplierQuotation selected = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));

        if (selected.getExpiryDate().isBefore(LocalDate.now())) {
            selected.setStatus(QuotationStatus.EXPIRED);
            quotationRepository.save(selected);
            throw new BusinessRuleException("Cannot select expired quotation. Expiry date was: " + selected.getExpiryDate());
        }

        selected.setStatus(QuotationStatus.SELECTED);
        SupplierQuotation saved = quotationRepository.save(selected);

        // Reject other quotations for same PR
        List<SupplierQuotation> others = quotationRepository.findByPurchaseRequestId(selected.getPurchaseRequestId());
        for (SupplierQuotation other : others) {
            if (!other.getId().equals(id) && other.getStatus() == QuotationStatus.RECEIVED) {
                other.setStatus(QuotationStatus.REJECTED);
                quotationRepository.save(other);
                recordAudit("QUOTATION", other.getId(), "REJECTED", "RECEIVED", "REJECTED", "MANAGER_" + managerId, "Alternate quotation selected");
            }
        }

        recordAudit("QUOTATION", saved.getId(), "SELECTED", "RECEIVED", "SELECTED", "MANAGER_" + managerId, "Quotation selected for PO creation");
        return mapToQuotationResponse(saved);
    }

    // --- Purchase Orders ---

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request, Long staffId) {
        log.info("Creating Purchase Order for Supplier ID: {}", request.getSupplierId());

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        if (supplier.getStatus() == SupplierStatus.BLACKLISTED) {
            throw new BusinessRuleException("Cannot create purchase order for blacklisted supplier: " + supplier.getName());
        }
        if (supplier.getStatus() == SupplierStatus.INACTIVE) {
            throw new BusinessRuleException("Cannot create purchase order for inactive supplier: " + supplier.getName());
        }

        String orderCode = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PurchaseOrder po = PurchaseOrder.builder()
                .orderCode(orderCode)
                .purchaseRequestId(request.getPurchaseRequestId())
                .quotationId(request.getQuotationId())
                .supplier(supplier)
                .orderDate(request.getOrderDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate() != null ? request.getExpectedDeliveryDate() : request.getOrderDate().plusWeeks(1))
                .status(OrderStatus.PENDING_APPROVAL)
                .paymentTerms(request.getPaymentTerms() != null ? request.getPaymentTerms() : supplier.getPaymentTerms())
                .shippingAddress(request.getShippingAddress() != null ? request.getShippingAddress() : "Hotel Grand Central Loading Dock, Bay #2")
                .createdByStaffId(staffId)
                .remarks(request.getRemarks())
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            validateInventoryItem(itemReq.getItemId());

            BigDecimal qty = new BigDecimal(itemReq.getOrderedQuantity());
            BigDecimal itemSub = itemReq.getUnitPrice().multiply(qty);

            BigDecimal taxRate = itemReq.getTaxRate() != null ? itemReq.getTaxRate() : BigDecimal.ZERO;
            BigDecimal taxAmt = itemSub.multiply(taxRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal itemTotal = itemSub.add(taxAmt);

            subtotal = subtotal.add(itemSub);
            totalTax = totalTax.add(taxAmt);

            PurchaseOrderItem poItem = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .itemId(itemReq.getItemId())
                    .itemName(itemReq.getItemName())
                    .orderedQuantity(itemReq.getOrderedQuantity())
                    .receivedQuantity(0)
                    .unitPrice(itemReq.getUnitPrice())
                    .taxRate(taxRate)
                    .totalAmount(itemTotal)
                    .build();

            po.addItem(poItem);
        }

        po.setSubtotal(subtotal);
        po.setDiscountAmount(BigDecimal.ZERO);
        po.setTaxAmount(totalTax);
        po.setGrandTotal(subtotal.add(totalTax));

        PurchaseOrder saved = purchaseOrderRepository.save(po);

        // Mark PR as CONVERTED_TO_ORDER if linked
        if (request.getPurchaseRequestId() != null) {
            purchaseRequestRepository.findById(request.getPurchaseRequestId()).ifPresent(pr -> {
                pr.setStatus(RequestStatus.CONVERTED_TO_ORDER);
                purchaseRequestRepository.save(pr);
            });
        }

        recordAudit("PURCHASE_ORDER", saved.getId(), "CREATED", null, "PENDING_APPROVAL", "STAFF_" + staffId, "PO created and submitted for approval");
        return mapToPurchaseOrderResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        return mapToPurchaseOrderResponse(po);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders(OrderStatus status) {
        if (status != null) {
            return purchaseOrderRepository.findByStatus(status).stream()
                    .map(this::mapToPurchaseOrderResponse)
                    .collect(Collectors.toList());
        }
        return purchaseOrderRepository.findAll().stream()
                .map(this::mapToPurchaseOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(Long id, ApprovePurchaseOrderRequest request) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        if (po.getStatus() != OrderStatus.PENDING_APPROVAL && po.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleException("Cannot approve PO with status: " + po.getStatus());
        }

        po.setStatus(OrderStatus.APPROVED);
        po.setApprovedByManagerId(request.getManagerId());
        po.setApprovedAt(LocalDateTime.now());
        if (request.getRemarks() != null) po.setRemarks(request.getRemarks());

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        recordAudit("PURCHASE_ORDER", saved.getId(), "APPROVED", "PENDING_APPROVAL", "APPROVED", "MANAGER_" + request.getManagerId(), request.getRemarks());

        eventPublisher.publishPurchaseOrderApproved(saved);
        return mapToPurchaseOrderResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse sendPurchaseOrderToSupplier(Long id, Long staffId) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        if (po.getStatus() != OrderStatus.APPROVED) {
            throw new BusinessRuleException("Cannot send PO to supplier unless it is in APPROVED status. Current status: " + po.getStatus());
        }

        po.setStatus(OrderStatus.SENT_TO_SUPPLIER);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        recordAudit("PURCHASE_ORDER", saved.getId(), "SENT", "APPROVED", "SENT_TO_SUPPLIER", "STAFF_" + staffId, "PO dispatched to vendor");
        return mapToPurchaseOrderResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(Long id, Long managerId, String remarks) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        if (po.getStatus() == OrderStatus.RECEIVED || po.getStatus() == OrderStatus.CLOSED) {
            throw new BusinessRuleException("Cannot cancel fulfilled PO with status: " + po.getStatus());
        }

        OrderStatus oldStatus = po.getStatus();
        po.setStatus(OrderStatus.CANCELLED);
        po.setRemarks(remarks);

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        recordAudit("PURCHASE_ORDER", saved.getId(), "CANCELLED", oldStatus.name(), "CANCELLED", "MANAGER_" + managerId, remarks);
        return mapToPurchaseOrderResponse(saved);
    }

    // --- Goods Receiving (GRN) ---

    @Override
    @Transactional
    public GoodsReceiptResponse recordGoodsReceipt(CreateGoodsReceiptRequest request) {
        log.info("Recording Goods Receipt for PO ID: {}", request.getPurchaseOrderId());

        PurchaseOrder po = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + request.getPurchaseOrderId()));

        if (po.getStatus() != OrderStatus.SENT_TO_SUPPLIER && po.getStatus() != OrderStatus.PARTIALLY_RECEIVED) {
            throw new BusinessRuleException("Cannot record goods receipt for PO in status: " + po.getStatus() + ". PO must be SENT_TO_SUPPLIER or PARTIALLY_RECEIVED.");
        }

        String receiptCode = "GRN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        GoodsReceipt receipt = GoodsReceipt.builder()
                .receiptCode(receiptCode)
                .purchaseOrderId(po.getId())
                .deliveryNoteNumber(request.getDeliveryNoteNumber())
                .receivedDate(request.getReceivedDate())
                .receivedByStaffId(request.getReceivedByStaffId())
                .vehicleNumber(request.getVehicleNumber())
                .remarks(request.getRemarks())
                .items(new ArrayList<>())
                .build();

        boolean allItemsFulfilled = true;

        for (GoodsReceiptItemRequest itemReq : request.getItems()) {
            PurchaseOrderItem poItem = po.getItems().stream()
                    .filter(pi -> pi.getId().equals(itemReq.getPurchaseOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("PO Line Item not found with id: " + itemReq.getPurchaseOrderItemId()));

            int acceptedQty = itemReq.getAcceptedQuantity() != null ? itemReq.getAcceptedQuantity() : 0;
            int rejectedQty = itemReq.getRejectedQuantity() != null ? itemReq.getRejectedQuantity() : 0;
            int totalReceived = itemReq.getReceivedQuantity() != null ? itemReq.getReceivedQuantity() : (acceptedQty + rejectedQty);

            if (acceptedQty + rejectedQty != totalReceived) {
                throw new BusinessRuleException("Accepted quantity (" + acceptedQty + ") + Rejected quantity (" + rejectedQty + ") must equal Total Received (" + totalReceived + ")");
            }

            int currentReceived = poItem.getReceivedQuantity() != null ? poItem.getReceivedQuantity() : 0;
            int newTotalReceived = currentReceived + acceptedQty;

            if (newTotalReceived > poItem.getOrderedQuantity()) {
                throw new BusinessRuleException("Accepted quantity exceeds ordered quantity for item: " + poItem.getItemName() + ". Ordered: " + poItem.getOrderedQuantity() + ", Already Received: " + currentReceived);
            }

            poItem.setReceivedQuantity(newTotalReceived);

            GoodsReceiptItem grnItem = GoodsReceiptItem.builder()
                    .goodsReceipt(receipt)
                    .purchaseOrderItemId(poItem.getId())
                    .itemId(poItem.getItemId())
                    .receivedQuantity(totalReceived)
                    .acceptedQuantity(acceptedQty)
                    .rejectedQuantity(rejectedQty)
                    .rejectionReason(itemReq.getRejectionReason())
                    .batchNumber(itemReq.getBatchNumber())
                    .expiryDate(itemReq.getExpiryDate())
                    .build();

            receipt.addItem(grnItem);
        }

        // Check if entire PO is fulfilled
        for (PurchaseOrderItem poItem : po.getItems()) {
            int received = poItem.getReceivedQuantity() != null ? poItem.getReceivedQuantity() : 0;
            if (received < poItem.getOrderedQuantity()) {
                allItemsFulfilled = false;
                break;
            }
        }

        OrderStatus newPoStatus = allItemsFulfilled ? OrderStatus.RECEIVED : OrderStatus.PARTIALLY_RECEIVED;
        po.setStatus(newPoStatus);
        purchaseOrderRepository.save(po);

        GoodsReceipt saved = goodsReceiptRepository.save(receipt);
        recordAudit("GOODS_RECEIPT", saved.getId(), "RECORDED", null, "COMPLETED", "STAFF_" + request.getReceivedByStaffId(), "GRN recorded. PO status updated to " + newPoStatus);

        // Publish event to RabbitMQ for inventory-service to auto-increment physical stock
        eventPublisher.publishGoodsReceived(saved);

        return mapToGoodsReceiptResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsReceiptResponse getGoodsReceiptById(Long id) {
        GoodsReceipt receipt = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goods receipt not found with id: " + id));
        return mapToGoodsReceiptResponse(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsReceiptResponse> getGoodsReceiptsByPurchaseOrder(Long poId) {
        return goodsReceiptRepository.findByPurchaseOrderId(poId).stream()
                .map(this::mapToGoodsReceiptResponse)
                .collect(Collectors.toList());
    }

    // --- Audit Logs ---

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseAuditLogResponse> getAuditLogs(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId).stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseAuditLogResponse> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---

    private void validateInventoryItem(Long itemId) {
        try {
            ItemResponse item = inventoryClient.getItemById(itemId);
            if (item == null) {
                log.warn("Inventory item with ID {} could not be verified via Feign, proceeding with standard check", itemId);
            }
        } catch (Exception ex) {
            log.warn("Could not reach inventory-service to validate item {}: {}", itemId, ex.getMessage());
        }
    }

    private void recordAudit(String entityType, Long entityId, String action, String oldState, String newState, String performedBy, String remarks) {
        try {
            PurchaseAuditLog logEntry = PurchaseAuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .oldState(oldState)
                    .newState(newState)
                    .performedBy(performedBy)
                    .remarks(remarks)
                    .build();
            auditLogRepository.save(logEntry);
        } catch (Exception ex) {
            log.error("Failed to record purchase audit log: {}", ex.getMessage());
        }
    }

    private SupplierResponse mapToSupplierResponse(Supplier s) {
        return SupplierResponse.builder()
                .id(s.getId())
                .supplierCode(s.getSupplierCode())
                .name(s.getName())
                .contactPerson(s.getContactPerson())
                .email(s.getEmail())
                .phone(s.getPhone())
                .address(s.getAddress())
                .taxNumber(s.getTaxNumber())
                .paymentTerms(s.getPaymentTerms())
                .status(s.getStatus())
                .rating(s.getRating())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private PurchaseRequestResponse mapToPurchaseRequestResponse(PurchaseRequest pr) {
        List<PurchaseRequestItemResponse> itemResponses = pr.getItems().stream()
                .map(i -> PurchaseRequestItemResponse.builder()
                        .id(i.getId())
                        .itemId(i.getItemId())
                        .itemName(i.getItemName())
                        .quantityRequested(i.getQuantityRequested())
                        .unitOfMeasure(i.getUnitOfMeasure())
                        .estimatedUnitPrice(i.getEstimatedUnitPrice())
                        .build())
                .collect(Collectors.toList());

        return PurchaseRequestResponse.builder()
                .id(pr.getId())
                .requestCode(pr.getRequestCode())
                .departmentId(pr.getDepartmentId())
                .requesterId(pr.getRequesterId())
                .requesterRole(pr.getRequesterRole())
                .reason(pr.getReason())
                .priority(pr.getPriority())
                .status(pr.getStatus())
                .requiredByDate(pr.getRequiredByDate())
                .approvedByManagerId(pr.getApprovedByManagerId())
                .approvedAt(pr.getApprovedAt())
                .rejectionReason(pr.getRejectionReason())
                .items(itemResponses)
                .createdAt(pr.getCreatedAt())
                .updatedAt(pr.getUpdatedAt())
                .build();
    }

    private SupplierQuotationResponse mapToQuotationResponse(SupplierQuotation q) {
        List<SupplierQuotationItemResponse> itemResponses = q.getItems().stream()
                .map(i -> SupplierQuotationItemResponse.builder()
                        .id(i.getId())
                        .itemId(i.getItemId())
                        .itemName(i.getItemName())
                        .quotedQuantity(i.getQuotedQuantity())
                        .unitPrice(i.getUnitPrice())
                        .discountPercentage(i.getDiscountPercentage())
                        .taxPercentage(i.getTaxPercentage())
                        .totalAmount(i.getTotalAmount())
                        .build())
                .collect(Collectors.toList());

        return SupplierQuotationResponse.builder()
                .id(q.getId())
                .quoteCode(q.getQuoteCode())
                .purchaseRequestId(q.getPurchaseRequestId())
                .supplierId(q.getSupplier().getId())
                .supplierName(q.getSupplier().getName())
                .quotationDate(q.getQuotationDate())
                .expiryDate(q.getExpiryDate())
                .paymentTerms(q.getPaymentTerms())
                .deliveryLeadTimeDays(q.getDeliveryLeadTimeDays())
                .subtotal(q.getSubtotal())
                .discountAmount(q.getDiscountAmount())
                .taxAmount(q.getTaxAmount())
                .grandTotal(q.getGrandTotal())
                .status(q.getStatus())
                .notes(q.getNotes())
                .items(itemResponses)
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }

    private PurchaseOrderResponse mapToPurchaseOrderResponse(PurchaseOrder po) {
        List<PurchaseOrderItemResponse> itemResponses = po.getItems().stream()
                .map(i -> PurchaseOrderItemResponse.builder()
                        .id(i.getId())
                        .itemId(i.getItemId())
                        .itemName(i.getItemName())
                        .orderedQuantity(i.getOrderedQuantity())
                        .receivedQuantity(i.getReceivedQuantity())
                        .remainingQuantity(i.getRemainingQuantity())
                        .unitPrice(i.getUnitPrice())
                        .taxRate(i.getTaxRate())
                        .totalAmount(i.getTotalAmount())
                        .build())
                .collect(Collectors.toList());

        return PurchaseOrderResponse.builder()
                .id(po.getId())
                .orderCode(po.getOrderCode())
                .purchaseRequestId(po.getPurchaseRequestId())
                .quotationId(po.getQuotationId())
                .supplierId(po.getSupplier().getId())
                .supplierName(po.getSupplier().getName())
                .orderDate(po.getOrderDate())
                .expectedDeliveryDate(po.getExpectedDeliveryDate())
                .status(po.getStatus())
                .paymentTerms(po.getPaymentTerms())
                .shippingAddress(po.getShippingAddress())
                .subtotal(po.getSubtotal())
                .discountAmount(po.getDiscountAmount())
                .taxAmount(po.getTaxAmount())
                .grandTotal(po.getGrandTotal())
                .createdByStaffId(po.getCreatedByStaffId())
                .approvedByManagerId(po.getApprovedByManagerId())
                .approvedAt(po.getApprovedAt())
                .remarks(po.getRemarks())
                .items(itemResponses)
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private GoodsReceiptResponse mapToGoodsReceiptResponse(GoodsReceipt grn) {
        List<GoodsReceiptItemResponse> itemResponses = grn.getItems().stream()
                .map(i -> GoodsReceiptItemResponse.builder()
                        .id(i.getId())
                        .purchaseOrderItemId(i.getPurchaseOrderItemId())
                        .itemId(i.getItemId())
                        .receivedQuantity(i.getReceivedQuantity())
                        .acceptedQuantity(i.getAcceptedQuantity())
                        .rejectedQuantity(i.getRejectedQuantity())
                        .rejectionReason(i.getRejectionReason())
                        .batchNumber(i.getBatchNumber())
                        .expiryDate(i.getExpiryDate())
                        .build())
                .collect(Collectors.toList());

        return GoodsReceiptResponse.builder()
                .id(grn.getId())
                .receiptCode(grn.getReceiptCode())
                .purchaseOrderId(grn.getPurchaseOrderId())
                .deliveryNoteNumber(grn.getDeliveryNoteNumber())
                .receivedDate(grn.getReceivedDate())
                .receivedByStaffId(grn.getReceivedByStaffId())
                .vehicleNumber(grn.getVehicleNumber())
                .remarks(grn.getRemarks())
                .items(itemResponses)
                .createdAt(grn.getCreatedAt())
                .updatedAt(grn.getUpdatedAt())
                .build();
    }

    private PurchaseAuditLogResponse mapToAuditLogResponse(PurchaseAuditLog l) {
        return PurchaseAuditLogResponse.builder()
                .id(l.getId())
                .entityType(l.getEntityType())
                .entityId(l.getEntityId())
                .action(l.getAction())
                .oldState(l.getOldState())
                .newState(l.getNewState())
                .performedBy(l.getPerformedBy())
                .remarks(l.getRemarks())
                .createdAt(l.getCreatedAt())
                .build();
    }
}
