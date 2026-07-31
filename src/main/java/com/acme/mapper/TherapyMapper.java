package com.acme.mapper;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.Professional;
import com.acme.domain.model.Therapy;
import com.acme.dto.request.create.CreateTherapyRequest;
import com.acme.dto.request.update.UpdateTherapyRequest;
import com.acme.dto.response.TherapyResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TherapyMapper {

    public Therapy toEntity(
            CreateTherapyRequest request,
            Dependent dependent,
            Professional professional
    ) {
        Therapy therapy = new Therapy();

        therapy.setDependent(dependent);
        therapy.setProfessional(professional);
        therapy.setDefaultSessionValue(
                request.defaultSessionValue()
        );
        therapy.setStartDate(request.startDate());
        therapy.setEndDate(request.endDate());
        therapy.setActive(true);
        therapy.setDescription(
                trimOrNull(request.description())
        );

        return therapy;
    }

    public Therapy updateEntity(
            UpdateTherapyRequest request,
            Therapy therapy
    ) {
        therapy.setDefaultSessionValue(
                request.defaultSessionValue()
        );
        therapy.setStartDate(request.startDate());
        therapy.setEndDate(request.endDate());
        therapy.setActive(request.active());
        therapy.setDescription(
                trimOrNull(request.description())
        );

        return therapy;
    }

    public TherapyResponse toResponse(
            Therapy therapy
    ) {
        Dependent dependent = therapy.getDependent();
        Professional professional =
                therapy.getProfessional();

        return new TherapyResponse(
                therapy.getId(),
                dependent.getId(),
                dependent.getName(),
                professional.getId(),
                professional.getName(),
                professional.getPixKey(),
                professional.getSpecialty().getId(),
                professional.getSpecialty().getName(),
                dependent.getFamily().getId(),
                dependent.getFamily().getName(),
                therapy.getDefaultSessionValue(),
                therapy.getStartDate(),
                therapy.getEndDate(),
                therapy.isActive(),
                therapy.getDescription()
        );
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}