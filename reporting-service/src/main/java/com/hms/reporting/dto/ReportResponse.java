package com.hms.reporting.dto;
import com.hms.reporting.entity.ReportType; import lombok.*; import java.math.*; import java.time.*; import java.util.*;
@Getter @Setter @Builder
public class ReportResponse { private UUID id; private ReportType reportType; private LocalDate reportDate; private String periodLabel; private BigDecimal value; private String summary; private OffsetDateTime createdAt; }