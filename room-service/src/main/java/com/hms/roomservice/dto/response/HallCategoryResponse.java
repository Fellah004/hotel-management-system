package com.hms.roomservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload for a hall category")
public class HallCategoryResponse {

    @Schema(description = "Hall category ID", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "BANQUET_HALL")
    private String name;

    @Schema(description = "Category description")
    private String description;

    @Schema(description = "Base hourly rate", example = "150.00")
    private BigDecimal basePricePerHour;

    @Schema(description = "Base daily rate", example = "1200.00")
    private BigDecimal basePricePerDay;

    @Schema(description = "Minimum capacity", example = "50")
    private Integer minCapacity;

    @Schema(description = "Maximum capacity", example = "500")
    private Integer maxCapacity;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
