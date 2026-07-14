package io.github.leturnos.panelvault.dto;

import io.github.leturnos.panelvault.model.WorkType;

public record WorkResponseDTO(
        Long id,
        String title,
        WorkType type,
        String publisher,
        String author,
        Integer totalVolumes,
        String coverUrl) {
}
