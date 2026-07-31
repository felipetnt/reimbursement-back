package com.acme.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateProfessionalRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String name,

        @NotBlank(message = "Chave PIX é obrigatória")
        @Size(max = 150, message = "Chave PIX deve ter no máximo 150 caracteres")
        String pixKey,

        @NotNull(message = "Especialidade é obrigatória")
        UUID specialtyId,

        @NotNull(message = "Família é obrigatória")
        UUID familyId
) {}
