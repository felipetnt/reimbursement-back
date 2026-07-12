package com.acme.dto.request.create;

import com.acme.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUserRequest(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String password,

        @NotNull(message = "Role é obrigatória")
        UserRole role,

        @NotNull(message = "Família é obrigatória")
        UUID familyId

) {}
