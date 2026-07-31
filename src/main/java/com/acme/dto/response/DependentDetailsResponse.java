package com.acme.dto.response;

import java.util.List;

public record DependentDetailsResponse(
        DependentResponse dependent,
        List<TherapyResponse> therapies,
        List<ReimbursementResponse> reimbursements
) {
}
