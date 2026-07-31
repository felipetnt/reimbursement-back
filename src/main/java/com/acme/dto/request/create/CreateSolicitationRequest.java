package com.acme.dto.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateSolicitationRequest(
        @NotNull(message = "Reembolso é obrigatório")
        UUID reimbursementId,

        @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
        String note
) {}
