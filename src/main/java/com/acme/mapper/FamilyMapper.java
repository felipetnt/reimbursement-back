package com.acme.mapper;

import com.acme.domain.model.Family;
import com.acme.dto.request.create.CreateFamilyRequest;
import com.acme.dto.request.update.UpdateFamilyRequest;
import com.acme.dto.response.FamilyResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FamilyMapper {

    public Family toEntity(CreateFamilyRequest request) {
        Family family = new Family();
        family.setName(request.name().trim());
        return family;
    }

    public Family updateEntity(UpdateFamilyRequest request, Family family) {
        family.setName(request.name().trim());
        return family;
    }

    public FamilyResponse toResponse(Family family) {
        return new FamilyResponse(family.getId(), family.getName());
    }
}
