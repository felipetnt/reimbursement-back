package com.acme.dto.response;

import java.util.List;

public record DashboardResponse(
        int year,
        int month,
        long totalReimbursements,
        long therapies,
        long professionals,
        List<DashboardStatusResponse> statuses,
        List<DashboardProfessionalResponse> professionalsSummary
) {
}