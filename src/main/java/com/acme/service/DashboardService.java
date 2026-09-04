package com.acme.service;

import com.acme.domain.enums.SolicitationStatus;
import com.acme.domain.enums.UserRole;
import com.acme.domain.model.Professional;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Solicitation;
import com.acme.dto.response.DashboardProfessionalResponse;
import com.acme.dto.response.DashboardResponse;
import com.acme.dto.response.DashboardSolicitationResponse;
import com.acme.dto.response.DashboardStatusResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class DashboardService {

    @Inject
    CurrentUserService currentUserService;

    @Inject
    FamilyAccessService familyAccessService;

    public DashboardResponse getDashboard(UUID requestedFamilyId, Integer requestedYear, Integer requestedMonth) {
        UUID familyId = resolveFamilyId(requestedFamilyId);
        YearMonth period = resolvePeriod(requestedYear, requestedMonth);
        LocalDate referenceMonth = period.atDay(1);

        List<Reimbursement> reimbursements = findReimbursements(familyId, referenceMonth);
        List<Solicitation> solicitations = findSolicitations(familyId, referenceMonth);

        Map<UUID, Solicitation> latestSolicitations = getLatestSolicitations(solicitations);

        EnumMap<SolicitationStatus, StatusAccumulator> statusAccumulators = createStatusAccumulators();
        Map<UUID, ProfessionalAccumulator> professionalAccumulators = new LinkedHashMap<>();
        Set<UUID> therapyIds = new HashSet<>();

        for (Reimbursement reimbursement : reimbursements) {
            Solicitation solicitation = latestSolicitations.get(reimbursement.getId());

            SolicitationStatus status = solicitation != null
                    ? solicitation.getStatus()
                    : SolicitationStatus.NOT_REQUESTED;

            DashboardSolicitationResponse dashboardSolicitation = toDashboardSolicitationResponse(reimbursement, solicitation, status);

            statusAccumulators.get(status).add(reimbursement.getTotalAmount(), dashboardSolicitation);

            therapyIds.add(reimbursement.getTherapy().getId());

            Professional professional = reimbursement.getTherapy().getProfessional();

            ProfessionalAccumulator professionalAccumulator = professionalAccumulators.computeIfAbsent(
                    professional.getId(),
                    id -> new ProfessionalAccumulator(professional)
            );

            professionalAccumulator.increment(status);
        }

        List<DashboardStatusResponse> statuses = buildStatusResponses(statusAccumulators);

        List<DashboardProfessionalResponse> professionalsSummary = professionalAccumulators
                .values()
                .stream()
                .map(ProfessionalAccumulator::toResponse)
                .sorted(Comparator.comparing(DashboardProfessionalResponse::professionalName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new DashboardResponse(
                period.getYear(),
                period.getMonthValue(),
                reimbursements.size(),
                therapyIds.size(),
                professionalAccumulators.size(),
                statuses,
                professionalsSummary
        );
    }

    private DashboardSolicitationResponse toDashboardSolicitationResponse(
            Reimbursement reimbursement,
            Solicitation solicitation,
            SolicitationStatus status
    ) {
        var therapy = reimbursement.getTherapy();
        var dependent = therapy.getDependent();
        var professional = therapy.getProfessional();
        var family = dependent.getFamily();

        return new DashboardSolicitationResponse(
                reimbursement.getId(),
                solicitation != null ? solicitation.getId() : null,
                solicitation != null ? solicitation.getAttemptNumber() : null,
                dependent.getId(),
                dependent.getName(),
                family.getId(),
                family.getName(),
                professional.getId(),
                professional.getName(),
                professional.getSpecialty().getName(),
                status,
                solicitation != null ? solicitation.getProtocolNumber() : null,
                solicitation != null ? solicitation.getRequestDate() : null,
                reimbursement.getTotalAmount()
        );
    }

    private YearMonth resolvePeriod(Integer requestedYear, Integer requestedMonth) {
        if (requestedYear == null && requestedMonth == null) {
            return YearMonth.now();
        }

        if (requestedYear == null || requestedMonth == null) {
            throw new WebApplicationException("Ano e mês devem ser informados juntos.", Response.Status.BAD_REQUEST);
        }

        try {
            return YearMonth.of(requestedYear, requestedMonth);
        } catch (DateTimeException exception) {
            throw new WebApplicationException("Ano ou mês inválido.", Response.Status.BAD_REQUEST);
        }
    }

    private UUID resolveFamilyId(UUID requestedFamilyId) {
        if (currentUserService.hasRole(UserRole.ADMIN)) {
            if (requestedFamilyId == null) {
                return null;
            }

            return familyAccessService.getAccessibleFamily(requestedFamilyId).getId();
        }

        UUID currentFamilyId = familyAccessService.getCurrentFamilyId();

        if (requestedFamilyId != null) {
            familyAccessService.ensureCanAccessFamily(requestedFamilyId);
        }

        return currentFamilyId;
    }

    private List<Reimbursement> findReimbursements(UUID familyId, LocalDate referenceMonth) {
        if (familyId == null) {
            return Reimbursement.list("referenceMonth = ?1 order by referenceMonth desc", referenceMonth);
        }

        return Reimbursement.list(
                "therapy.dependent.family.id = ?1 and referenceMonth = ?2 order by referenceMonth desc",
                familyId,
                referenceMonth
        );
    }

    private List<Solicitation> findSolicitations(UUID familyId, LocalDate referenceMonth) {
        if (familyId == null) {
            return Solicitation.list(
                    "reimbursement.referenceMonth = ?1 order by attemptNumber desc",
                    referenceMonth
            );
        }

        return Solicitation.list(
                "reimbursement.therapy.dependent.family.id = ?1 and reimbursement.referenceMonth = ?2 order by attemptNumber desc",
                familyId,
                referenceMonth
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

    private List<DashboardStatusResponse> buildStatusResponses(EnumMap<SolicitationStatus, StatusAccumulator> accumulators) {
        List<DashboardStatusResponse> responses = new ArrayList<>();

        for (SolicitationStatus status : SolicitationStatus.values()) {
            StatusAccumulator accumulator = accumulators.get(status);

            responses.add(new DashboardStatusResponse(
                    status,
                    accumulator.quantity,
                    accumulator.totalAmount,
                    List.copyOf(accumulator.solicitations)
            ));
        }

        return responses;
    }

    private static class StatusAccumulator {

        private long quantity;
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private final List<DashboardSolicitationResponse> solicitations = new ArrayList<>();

        void add(BigDecimal amount, DashboardSolicitationResponse solicitation) {
            quantity++;

            if (amount != null) {
                totalAmount = totalAmount.add(amount);
            }

            solicitations.add(solicitation);
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
            return notRequested + underReview + authorized + reimbursed + denied;
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