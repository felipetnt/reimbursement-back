package com.acme.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTherapyTypeRequest(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotNull(message = "Dependente é obrigatório")
        UUID dependentId

) {
}