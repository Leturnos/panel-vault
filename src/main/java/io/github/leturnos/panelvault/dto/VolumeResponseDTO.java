package io.github.leturnos.panelvault.dto;

import io.github.leturnos.panelvault.model.Work;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VolumeResponseDTO(
        Long id,
        Integer number,
        LocalDate purchaseDate,
        BigDecimal purchasePrice,
        Boolean owned,
        Work work) {
}
