package com.acme.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InitializeSystemRequest(

        @NotBlank(message = "Nome da família é obrigatório")
        @Size(
                max = 100,
                message = "Nome da família deve ter no máximo 100 caracteres"
        )
        String familyName,

        @NotBlank(message = "Nome do administrador é obrigatório")
        @Size(
                max = 100,
                message = "Nome do administrador deve ter no máximo 100 caracteres"
        )
        String adminName,

        @NotBlank(message = "E-mail do administrador é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(
                max = 150,
                message = "E-mail deve ter no máximo 150 caracteres"
        )
        String adminEmail,

        @NotBlank(message = "Senha é obrigatória")
        @Size(
                min = 8,
                max = 72,
                message = "Senha deve possuir entre 8 e 72 caracteres"
        )
        String adminPassword

) {
}
