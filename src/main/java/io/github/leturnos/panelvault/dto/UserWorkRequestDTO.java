package io.github.leturnos.panelvault.dto;

import io.github.leturnos.panelvault.model.WorkStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UserWorkRequestDTO(
        @NotNull(message = "O status de coleção é obrigatório")
        WorkStatus status,

        @DecimalMin(value = "0.0", message = "A nota mínima é 0.0")
        @DecimalMax(value = "10.0", message = "A nota máxima é 10.0")
        BigDecimal rating
) {
    @AssertTrue(message = "A nota deve ser em passos de 0.5 (ex: 8.0, 8.5, 9.0)")
    public boolean isRatingStepValid() {
        if (rating == null) {
            return true; // if the user doesn't want to give a rating
        }

        return rating.remainder(BigDecimal.valueOf(0.5)).compareTo(BigDecimal.ZERO) == 0;
    }
}

