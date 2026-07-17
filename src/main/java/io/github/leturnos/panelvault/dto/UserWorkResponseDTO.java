package io.github.leturnos.panelvault.dto;

import io.github.leturnos.panelvault.model.WorkStatus;
import java.math.BigDecimal;

public record UserWorkResponseDTO(
        Long id,
        Long workId,
        String workTitle,
        WorkStatus status,
        BigDecimal rating
) {}
