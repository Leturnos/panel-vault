package io.github.leturnos.panelvault.dto;

import java.math.BigDecimal;

public interface MonthlyExpenseProjection {
    Integer getYear();
    Integer getMonth();
    BigDecimal getTotalSpent();
    Long getVolumeCount();
}
