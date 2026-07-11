package io.github.leturnos.panelvault.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VolumeRequestDTO(
        @NotNull(message = "O número do volume é obrigatório")
        @Positive(message = "O número do volume deve ser positivo")
        Integer number,

        @PastOrPresent(message = "A data de compra não pode ser no futuro")
        LocalDate purchaseDate,

        @DecimalMin(value = "0", message = "O preço de compra não pode ser negativo")
        BigDecimal purchasePrice,

        @NotNull(message = "O campo owned é obrigatório")
        Boolean owned
) {
}
