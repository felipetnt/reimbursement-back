package com.acme.service;

import com.acme.domain.model.Family;
import com.acme.dto.request.create.CreateFamilyRequest;
import com.acme.dto.request.update.UpdateFamilyRequest;
import com.acme.dto.response.FamilyResponse;
import com.acme.mapper.FamilyMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class FamilyService {

    @Inject
    private FamilyMapper mapper;

    @Transactional
    public FamilyResponse create(CreateFamilyRequest request) {
        Family family = mapper.toEntity(request);

        family.persist();

        return mapper.toResponse(family);
    }

    @Transactional
    public FamilyResponse update(UUID id, UpdateFamilyRequest request) {
        Family familyUpdated = Family.findById(id);

        nullFamilyCheck(familyUpdated);

        return mapper.toResponse(mapper.updateEntity(request, familyUpdated));
    }

    @Transactional
    public void delete(UUID id) {
        Family familyToDelete = Family.findById(id);

        nullFamilyCheck(familyToDelete);

        familyToDelete.delete();
    }

    public FamilyResponse getFamilyById(UUID id) {
        Family family = Family.findById(id);

        nullFamilyCheck(family);

        return mapper.toResponse(family);
    }

    public List<FamilyResponse> readAll() {
        return Family.<Family>listAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    private void nullFamilyCheck(Family family) {
        if (family == null) {
            throw new WebApplicationException(
                    "Família não encontrada.",
                    Response.Status.NOT_FOUND
            );
        }
    }
}