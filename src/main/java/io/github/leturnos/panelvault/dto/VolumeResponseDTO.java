package io.github.leturnos.panelvault.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VolumeResponseDTO(
        Long id,
        Integer number,
        LocalDate purchaseDate,
        BigDecimal purchasePrice,
        Boolean owned,
        Long workId) {
}
