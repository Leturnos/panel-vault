package io.github.leturnos.panelvault.dto;

import java.math.BigDecimal;

public record WorkFinancialResponseDTO(
        Long workId,
        String workTitle,
        BigDecimal totalSpent,
        Integer ownedVolumesCount,
        Integer totalVolumes,
        BigDecimal averagePricePaid,
        BigDecimal estimatedRemainingCost
) {
}
