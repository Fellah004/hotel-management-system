package com.hms.billing.dto;
import com.hms.billing.entity.InvoiceStatus; import lombok.*; import java.math.*; import java.time.*; import java.util.*;
@Getter @Setter @Builder
public class InvoiceResponse { private UUID id; private String invoiceNumber; private UUID reservationId; private UUID guestId; private InvoiceStatus status; private BigDecimal subtotal,taxAmount,discountAmount,totalAmount; private List<InvoiceItemResponse> items; private OffsetDateTime createdAt; }