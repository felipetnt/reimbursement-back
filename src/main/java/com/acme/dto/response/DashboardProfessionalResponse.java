package com.acme.dto.response;

import java.util.UUID;

public record DashboardProfessionalResponse(
        UUID professionalId,
        String professionalName,
        String specialtyName,
        long total,
        long notRequested,
        long underReview,
        long authorized,
        long reimbursed,
        long denied
) {
}