package com.hms.purchaseservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_request_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id", nullable = false)
    @JsonIgnore
    private PurchaseRequest purchaseRequest;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false)
    private Integer quantityRequested;

    @Column(length = 30)
    private String unitOfMeasure;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedUnitPrice;
}
