package com.acme.dto.response;

import java.util.List;

public record DashboardResponse(
        long totalReimbursements,
        long activeTherapies,
        long professionals,
        List<DashboardStatusResponse> statuses,
        List<DashboardProfessionalResponse> professionalsSummary
) {
}