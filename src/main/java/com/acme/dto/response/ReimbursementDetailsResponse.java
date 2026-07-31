package com.acme.dto.response;

import java.util.List;

public record ReimbursementDetailsResponse(
        ReimbursementResponse reimbursement,
        List<SolicitationResponse> solicitations
) {
}
