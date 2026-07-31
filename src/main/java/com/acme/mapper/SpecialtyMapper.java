package com.acme.mapper;

import com.acme.domain.model.Family;
import com.acme.domain.model.Specialty;
import com.acme.dto.request.create.CreateSpecialtyRequest;
import com.acme.dto.request.update.UpdateSpecialtyRequest;
import com.acme.dto.response.SpecialtyResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SpecialtyMapper {

    public Specialty toEntity(
            CreateSpecialtyRequest request,
            Family family
    ) {
        Specialty specialty = new Specialty();
        specialty.setName(request.name().trim());
        specialty.setFamily(family);
        return specialty;
    }

    public Specialty updateEntity(
            UpdateSpecialtyRequest request,
            Specialty specialty
    ) {
        specialty.setName(request.name().trim());
        return specialty;
    }

    public SpecialtyResponse toResponse(Specialty specialty) {
        return new SpecialtyResponse(
                specialty.getId(),
                specialty.getName(),
                specialty.getFamily().getId(),
                specialty.getFamily().getName()
        );
    }
}
