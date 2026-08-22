package io.github.leturnos.panelvault.dto;

import java.math.BigDecimal;

public record FinancialSummaryResponseDTO(
        BigDecimal totalSpent,
        Integer totalVolumesWithPrice,
        BigDecimal averageVolumePrice,
        BigDecimal highestPrice,
        BigDecimal lowestPrice,
        BigDecimal estimatedToCompleteCollection
) {
}
