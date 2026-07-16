package io.github.leturnos.panelvault.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Username é obrigatório")
        @Size(max = 50, message = "O username não pode ter mais que 50 caracteres")
        String username,

        @Email(message = "Email deve ser válido")
        @NotBlank(message = "Email é obrigatório")
        @Size(max = 100, message = "O email não pode ter mais que 100 caracteres")
        String email,

        @NotBlank(message = "Password é obrigatório")
        @Size(
                max = 50, min = 8,
                message = "O password deve ter entre 8 e 50 caracteres"
        )
        String password
) {
}
