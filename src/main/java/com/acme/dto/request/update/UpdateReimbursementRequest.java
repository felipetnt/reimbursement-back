package com.acme.dto.request.update;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateReimbursementRequest(
        @NotNull(message = "Mês de referência é obrigatório")
        @PastOrPresent(message = "Mês de referência não pode estar no futuro")
        LocalDate referenceMonth,

        @NotNull(message = "Quantidade de sessões é obrigatória")
        @Positive(message = "Quantidade de sessões deve ser maior que zero")
        Integer sessionsQuantity,

        @NotNull(message = "Valor da sessão é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor da sessão deve ser maior que zero")
        BigDecimal sessionValue,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String description
) {}
