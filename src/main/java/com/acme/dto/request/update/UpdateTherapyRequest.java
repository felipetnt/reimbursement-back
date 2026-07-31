package com.acme.dto.request.update;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTherapyRequest(

        @NotNull(message = "Valor padrão da sessão é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "Valor padrão da sessão deve ser maior que zero"
        )
        BigDecimal defaultSessionValue,

        @NotNull(message = "Data de início é obrigatória")
        LocalDate startDate,

        LocalDate endDate,

        @NotNull(message = "Situação ativa é obrigatória")
        Boolean active,

        @Size(
                max = 500,
                message = "Descrição deve ter no máximo 500 caracteres"
        )
        String description

) {
}