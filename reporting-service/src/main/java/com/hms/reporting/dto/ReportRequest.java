package com.hms.reporting.dto;
import com.hms.reporting.entity.ReportType; import jakarta.validation.constraints.NotNull; import lombok.*; import java.math.*; import java.time.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReportRequest { @NotNull private ReportType reportType; @NotNull private LocalDate reportDate; private String periodLabel; private BigDecimal value; private String summary; }