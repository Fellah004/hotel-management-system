package com.hms.billing.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*; import java.util.*;
@Entity @Table(name="bill_items") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BillItem {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="invoice_id",nullable=false) private Invoice invoice;
 @Column(nullable=false,length=300) private String description; @Column(nullable=false) private Integer quantity;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal unitPrice; @Column(nullable=false,precision=12,scale=2) private BigDecimal lineTotal;
}