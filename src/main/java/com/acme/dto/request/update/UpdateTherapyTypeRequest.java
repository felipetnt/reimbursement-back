package com.acme.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateTherapyTypeRequest(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotNull(message = "Dependente é obrigatório")
        UUID dependentId

) {
}
