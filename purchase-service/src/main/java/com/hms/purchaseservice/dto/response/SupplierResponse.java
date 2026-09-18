package com.hms.purchaseservice.dto.response;

import com.hms.purchaseservice.entity.PaymentTerms;
import com.hms.purchaseservice.entity.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private Long id;
    private String supplierCode;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String taxNumber;
    private PaymentTerms paymentTerms;
    private SupplierStatus status;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
