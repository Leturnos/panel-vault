package io.github.leturnos.panelvault.dto;

import java.math.BigDecimal;

public record MonthlyExpenseResponseDTO(
        Integer year,
        Integer month,
        BigDecimal totalSpent,
        Long volumeCount
) {
}
