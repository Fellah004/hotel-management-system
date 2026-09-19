package com.hms.purchaseservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "goods_receipt_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    @JsonIgnore
    private GoodsReceipt goodsReceipt;

    @Column(nullable = false)
    private Long purchaseOrderItemId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Integer receivedQuantity;

    @Column(nullable = false)
    private Integer acceptedQuantity;

    @Column(nullable = false)
    @Builder.Default
    private Integer rejectedQuantity = 0;

    @Column(length = 255)
    private String rejectionReason;

    @Column(length = 50)
    private String batchNumber;

    private LocalDate expiryDate;
}
