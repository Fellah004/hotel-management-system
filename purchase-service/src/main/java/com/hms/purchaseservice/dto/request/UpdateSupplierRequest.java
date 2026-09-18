package com.hms.purchaseservice.dto.request;

import com.hms.purchaseservice.entity.PaymentTerms;
import com.hms.purchaseservice.entity.SupplierStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private String contactPerson;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String address;
    private String taxNumber;
    private PaymentTerms paymentTerms;
    private SupplierStatus status;
    private BigDecimal rating;
}
