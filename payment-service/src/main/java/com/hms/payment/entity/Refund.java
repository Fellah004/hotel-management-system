package com.hms.payment.entity;
import jakarta.persistence.*; import lombok.*; import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name="refunds") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Refund {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false) private UUID paymentId;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 @Column(nullable=false,length=500) private String reason;
 @Column(length=120) private String providerReference;
 @CreationTimestamp @Column(nullable=false,updatable=false) private OffsetDateTime createdAt;
}