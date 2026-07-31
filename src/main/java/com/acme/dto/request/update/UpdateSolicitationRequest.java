package com.acme.dto.request.update;

import com.acme.domain.enums.SolicitationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateSolicitationRequest(
        @NotNull(message = "Status é obrigatório")
        SolicitationStatus status,

        @Size(max = 100, message = "Protocolo deve ter no máximo 100 caracteres")
        String protocolNumber,

        @PastOrPresent(message = "Data da solicitação não pode estar no futuro")
        LocalDate requestDate,

        @PastOrPresent(message = "Data do reembolso não pode estar no futuro")
        LocalDate reimbursementDate,

        @PositiveOrZero(message = "Valor recebido não pode ser negativo")
        BigDecimal amountReceived,

        @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
        String note
) {}
