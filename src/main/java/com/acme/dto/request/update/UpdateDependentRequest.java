package com.acme.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateDependentRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String name,

        @NotNull(message = "Data de nascimento é obrigatória")
        @PastOrPresent(message = "Data de nascimento não pode estar no futuro")
        LocalDate birthDate
) {}
