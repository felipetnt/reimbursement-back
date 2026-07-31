package com.acme.mapper;

import com.acme.domain.model.Family;
import com.acme.domain.model.Professional;
import com.acme.domain.model.Specialty;
import com.acme.dto.request.create.CreateProfessionalRequest;
import com.acme.dto.request.update.UpdateProfessionalRequest;
import com.acme.dto.response.ProfessionalResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProfessionalMapper {

    public Professional toEntity(
            CreateProfessionalRequest request,
            Family family,
            Specialty specialty
    ) {
        Professional professional = new Professional();
        professional.setName(request.name().trim());
        professional.setPixKey(request.pixKey().trim());
        professional.setFamily(family);
        professional.setSpecialty(specialty);
        return professional;
    }

    public Professional updateEntity(
            UpdateProfessionalRequest request,
            Professional professional,
            Specialty specialty
    ) {
        professional.setName(request.name().trim());
        professional.setPixKey(request.pixKey().trim());
        professional.setSpecialty(specialty);
        return professional;
    }

    public ProfessionalResponse toResponse(Professional professional) {
        return new ProfessionalResponse(
                professional.getId(),
                professional.getName(),
                professional.getPixKey(),
                professional.getSpecialty().getId(),
                professional.getSpecialty().getName(),
                professional.getFamily().getId(),
                professional.getFamily().getName()
        );
    }
}
