package com.hms.reporting.entity;
import jakarta.persistence.*; import lombok.*; import org.hibernate.annotations.CreationTimestamp; import java.math.*; import java.time.*; import java.util.*;
@Entity @Table(name="report_snapshots") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportSnapshot {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=40) private ReportType reportType;
 @Column(nullable=false) private LocalDate reportDate; @Column(length=100) private String periodLabel;
 @Column(precision=14,scale=2) private BigDecimal value; @Column(length=5000) private String summary;
 @CreationTimestamp @Column(nullable=false,updatable=false) private OffsetDateTime createdAt;
}