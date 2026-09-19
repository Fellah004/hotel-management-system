package com.hms.purchaseservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String entityType; // PURCHASE_REQUEST, QUOTATION, PURCHASE_ORDER, GOODS_RECEIPT

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 50)
    private String action; // CREATED, SUBMITTED, APPROVED, REJECTED, SENT, GRN_RECORDED, CANCELLED

    @Column(length = 50)
    private String oldState;

    @Column(length = 50)
    private String newState;

    @Column(nullable = false, length = 50)
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
