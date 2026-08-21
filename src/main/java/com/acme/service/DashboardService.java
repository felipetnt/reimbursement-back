package com.acme.service;

import com.acme.domain.enums.SolicitationStatus;
import com.acme.domain.enums.UserRole;
import com.acme.domain.model.Professional;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Solicitation;
import com.acme.domain.model.Therapy;
import com.acme.dto.response.DashboardProfessionalResponse;
import com.acme.dto.response.DashboardResponse;
import com.acme.dto.response.DashboardStatusResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class DashboardService {

    @Inject
    CurrentUserService currentUserService;

    @Inject
    FamilyAccessService familyAccessService;

    public DashboardResponse getDashboard(UUID requestedFamilyId) {
        UUID familyId = resolveFamilyId(requestedFamilyId);

        List<Reimbursement> reimbursements = findReimbursements(familyId);
        List<Solicitation> solicitations = findSolicitations(familyId);
        List<Professional> professionals = findProfessionals(familyId);

        Map<UUID, Solicitation> latestSolicitations = getLatestSolicitations(solicitations);

        EnumMap<SolicitationStatus, StatusAccumulator> statusAccumulators = createStatusAccumulators();

        Map<UUID, ProfessionalAccumulator> professionalAccumulators = createProfessionalAccumulators(professionals);

        for (Reimbursement reimbursement : reimbursements) {
            Solicitation solicitation = latestSolicitations.get(reimbursement.getId());

            SolicitationStatus status = solicitation != null ? solicitation.getStatus() : SolicitationStatus.NOT_REQUESTED;

            statusAccumulators.get(status).add(reimbursement.getTotalAmount());

            Professional professional = reimbursement.getTherapy().getProfessional();

            ProfessionalAccumulator professionalAccumulator =
                    professionalAccumulators.computeIfAbsent(professional.getId(), id -> new ProfessionalAccumulator(professional));

            professionalAccumulator.increment(status);
        }

        List<DashboardStatusResponse> statuses = buildStatusResponses(statusAccumulators);

        List<DashboardProfessionalResponse> professionalsSummary = professionalAccumulators
                        .values()
                        .stream()
                        .map(ProfessionalAccumulator::toResponse)
                        .toList();

        long activeTherapies = countActiveTherapies(familyId);

        return new DashboardResponse(
                reimbursements.size(),
                activeTherapies,
                professionals.size(),
                statuses,
                professionalsSummary
        );
    }

    private UUID resolveFamilyId(UUID requestedFamilyId) {
        if (currentUserService.hasRole(UserRole.ADMIN)) {
            if (requestedFamilyId == null) {
                return null;
            }

            return familyAccessService
                    .getAccessibleFamily(requestedFamilyId)
                    .getId();
        }

        UUID currentFamilyId =
                familyAccessService.getCurrentFamilyId();

        if (requestedFamilyId != null) {
            familyAccessService.ensureCanAccessFamily(requestedFamilyId);
        }

        return currentFamilyId;
    }

    private List<Reimbursement> findReimbursements(UUID familyId) {
        if (familyId == null) {
            return Reimbursement.list(
                    "order by referenceMonth desc"
            );
        }

        return Reimbursement.list(
                "therapy.dependent.family.id = ?1 order by referenceMonth desc",
                familyId
        );
    }

    private List<Solicitation> findSolicitations(UUID familyId) {
        if (familyId == null) {
            return Solicitation.list(
                    "order by attemptNumber desc"
            );
        }

        return Solicitation.list(
                "reimbursement.therapy.dependent.family.id = ?1 order by attemptNumber desc",
                familyId
        );
    }

    private List<Professional> findProfessionals(UUID familyId) {
        if (familyId == null) {
            return Professional.list(
                    "order by name"
            );
        }

        return Professional.list(
                "family.id = ?1 order by name",
                familyId
        );
    }

    private long countActiveTherapies(UUID familyId) {
        if (familyId == null) {
            return Therapy.count(
                    "active = true"
            );
        }

        return Therapy.count(
                "active = true and dependent.family.id = ?1",
                familyId
        );
    }

    private Map<UUID, Solicitation> getLatestSolicitations(List<Solicitation> solicitations) {
        Map<UUID, Solicitation> latest = new HashMap<>();

        for (Solicitation solicitation : solicitations) {
            UUID reimbursementId = solicitation.getReimbursement().getId();

            Solicitation current = latest.get(reimbursementId);

            if (current == null || solicitation.getAttemptNumber() > current.getAttemptNumber()) {
                latest.put(reimbursementId, solicitation);
            }
        }

        return latest;
    }

    private EnumMap<SolicitationStatus, StatusAccumulator> createStatusAccumulators() {
        EnumMap<SolicitationStatus, StatusAccumulator> accumulators = new EnumMap<>(SolicitationStatus.class);

        for (SolicitationStatus status : SolicitationStatus.values()) {
            accumulators.put(status, new StatusAccumulator());
        }

        return accumulators;
    }

    private Map<UUID, ProfessionalAccumulator> createProfessionalAccumulators(List<Professional> professionals) {
        Map<UUID, ProfessionalAccumulator> accumulators = new LinkedHashMap<>();

        for (Professional professional : professionals) {
            accumulators.put(professional.getId(), new ProfessionalAccumulator(professional));
        }

        return accumulators;
    }

    private List<DashboardStatusResponse> buildStatusResponses(EnumMap<SolicitationStatus, StatusAccumulator> accumulators) {
        List<DashboardStatusResponse> responses = new ArrayList<>();

        for (SolicitationStatus status : SolicitationStatus.values()) {
            StatusAccumulator accumulator = accumulators.get(status);

            responses.add(new DashboardStatusResponse(status, accumulator.quantity, accumulator.totalAmount));
        }

        return responses;
    }

    private static class StatusAccumulator {

        private long quantity;

        private BigDecimal totalAmount =
                BigDecimal.ZERO;

        void add(BigDecimal amount) {
            quantity++;

            if (amount != null) {
                totalAmount =
                        totalAmount.add(amount);
            }
        }
    }

    private static class ProfessionalAccumulator {

        private final UUID professionalId;
        private final String professionalName;
        private final String specialtyName;

        private long notRequested;
        private long underReview;
        private long authorized;
        private long reimbursed;
        private long denied;

        ProfessionalAccumulator(Professional professional) {
            this.professionalId = professional.getId();

            this.professionalName = professional.getName();

            this.specialtyName = professional.getSpecialty().getName();
        }

        void increment(SolicitationStatus status) {
            switch (status) {
                case NOT_REQUESTED -> notRequested++;
                case UNDER_REVIEW -> underReview++;
                case AUTHORIZED -> authorized++;
                case REIMBURSED -> reimbursed++;
                case DENIED -> denied++;
            }
        }

        long total() {
            return notRequested
                    + underReview
                    + authorized
                    + reimbursed
                    + denied;
        }

        DashboardProfessionalResponse toResponse() {
            return new DashboardProfessionalResponse(
                    professionalId,
                    professionalName,
                    specialtyName,
                    total(),
                    notRequested,
                    underReview,
                    authorized,
                    reimbursed,
                    denied
            );
        }
    }
}