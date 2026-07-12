package com.acme.dto.request.update;

import jakarta.validation.constraints.NotBlank;

public record UpdateFamilyRequest(

        @NotBlank(message = "Nome é obrigatório")
        String name

) {
}
