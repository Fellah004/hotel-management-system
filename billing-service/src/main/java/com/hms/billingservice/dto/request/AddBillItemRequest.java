package com.hms.billingservice.dto.request;

import com.hms.billingservice.entity.BillItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddBillItemRequest {

    @NotBlank(message = "Description is mandatory")
    private String description;

    @NotNull(message = "Item type is mandatory")
    private BillItemType itemType;

    @NotNull(message = "Quantity is mandatory")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Unit price is mandatory")
    private BigDecimal unitPrice;
}
