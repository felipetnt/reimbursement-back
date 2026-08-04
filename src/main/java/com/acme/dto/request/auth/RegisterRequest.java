package com.acme.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Nome da família é obrigatório")
        @Size(max = 100, message = "Nome da família deve ter no máximo 100 caracteres")
        String familyName,

        @NotBlank(message = "Nome do responsável é obrigatório")
        @Size(max = 100, message = "Nome do responsável deve ter no máximo 100 caracteres")
        String name,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 72, message = "Senha deve possuir entre 8 e 72 caracteres")
        String password,

        @NotBlank(message = "Confirmação de senha é obrigatória")
        String confirmPassword

) {
}
