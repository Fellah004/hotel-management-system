package com.hms.billing.dto;
import lombok.*; import java.math.*; import java.util.*;
@Getter @Setter @Builder
public class InvoiceItemResponse { private UUID id; private String description; private Integer quantity; private BigDecimal unitPrice,lineTotal; }