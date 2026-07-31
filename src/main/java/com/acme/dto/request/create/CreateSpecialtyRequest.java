package com.acme.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateSpecialtyRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String name,

        @NotNull(message = "Família é obrigatória")
        UUID familyId
) {}
