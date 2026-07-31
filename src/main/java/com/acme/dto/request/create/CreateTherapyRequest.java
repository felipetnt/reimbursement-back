package com.acme.dto.request.create;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTherapyRequest(
        @NotNull(message = "Dependente é obrigatório")
        UUID dependentId,

        @NotNull(message = "Profissional é obrigatório")
        UUID professionalId,

        @NotNull(message = "Valor padrão da sessão é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor padrão da sessão deve ser maior que zero")
        BigDecimal defaultSessionValue,

        @NotNull(message = "Data de início é obrigatória")
        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String description
) {}
