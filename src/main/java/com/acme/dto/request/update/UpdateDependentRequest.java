package com.acme.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateDependentRequest(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @PastOrPresent(message = "Data de nascimento não pode estar no futuro")
        LocalDate birthDate,

        @NotNull(message = "Família é obrigatória")
        UUID familyId

) {
}
