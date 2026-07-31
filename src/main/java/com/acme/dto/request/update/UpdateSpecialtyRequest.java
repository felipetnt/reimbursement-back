package com.acme.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSpecialtyRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String name
) {}
