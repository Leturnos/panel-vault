package io.github.leturnos.panelvault.dto;

import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.model.WorkType;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

public record WorkRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 255, message = "O título não pode ter mais que 255 caracteres")
        String title,

        @NotNull(message = "O tipo da obra é obrigatório")
        WorkType type,

        @Size(max = 100, message = "A editora não pode ter mais que 100 caracteres")
        String publisher,

        @Size(max = 100, message = "O autor não pode ter mais que 100 caracteres")
        String author,

        @Positive(message = "A quantidade de volumes deve ser positiva")
        Integer totalVolumes,

        @NotNull(message = "O status da obra é obrigatório")
        WorkStatus status,

        @URL(message = "A URL da capa deve ser válida")
        @Size(max = 500, message = "A URL da capa não pode ter mais que 500 caracteres")
        String coverUrl) {
}
