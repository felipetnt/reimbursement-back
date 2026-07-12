package com.acme.dto.request.update;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateReimbursementRequest(

        @NotNull(message = "Dependente é obrigatório")
        UUID dependentId,

        @NotNull(message = "Tipo de terapia é obrigatório")
        UUID therapyTypeId,

        @NotNull(message = "Mês de referência é obrigatório")
        @PastOrPresent(message = "Mês de referência não pode estar no futuro")
        LocalDate referenceMonth,

        @NotNull(message = "Quantidade de sessões é obrigatória")
        @Positive(message = "Quantidade de sessões deve ser maior que zero")
        Integer sessionsQuantity,

        @NotNull(message = "Valor da sessão é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor da sessão deve ser maior que zero")
        BigDecimal sessionValue,

        @NotBlank(message = "Nome da terapeuta é obrigatório")
        @Size(max = 100, message = "Nome da terapeuta deve ter no máximo 100 caracteres")
        String therapistName,

        @NotBlank(message = "PIX da terapeuta é obrigatório")
        @Size(max = 150, message = "PIX da terapeuta deve ter no máximo 150 caracteres")
        String therapistPix,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String description

) {
}
