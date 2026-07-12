package com.acme.service;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.Family;
import com.acme.dto.request.create.CreateDependentRequest;
import com.acme.dto.request.update.UpdateDependentRequest;
import com.acme.dto.response.DependentResponse;
import com.acme.mapper.DependentMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class DependentService {

    @Inject
    DependentMapper mapper;

    public List<DependentResponse> list() {
        return Dependent.<Dependent>listAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public DependentResponse findById(UUID id) {
        Dependent dependent = Dependent.findById(id);

        ensureDependentExists(dependent);

        return mapper.toResponse(dependent);
    }

    public List<DependentResponse> listByFamily(UUID familyId) {
        Family family = Family.findById(familyId);

        ensureFamilyExists(family);

        return Dependent.<Dependent>list("family.id", familyId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DependentResponse create(CreateDependentRequest request) {
        Family family = Family.findById(request.familyId());

        ensureFamilyExists(family);

        Dependent dependent = mapper.toEntity(request, family);

        dependent.persist();

        return mapper.toResponse(dependent);
    }

    @Transactional
    public DependentResponse update(UUID id, UpdateDependentRequest request) {
        Dependent dependentBeforeUpdate = Dependent.findById(id);

        ensureDependentExists(dependentBeforeUpdate);

        Family family = Family.findById(request.familyId());

        ensureFamilyExists(family);

        Dependent dependent = mapper.updateEntity(request, dependentBeforeUpdate, family);

        return mapper.toResponse(dependent);
    }

    @Transactional
    public void delete(UUID id) {
        Dependent dependent = Dependent.findById(id);

        ensureDependentExists(dependent);

        dependent.delete();
    }

    private void ensureFamilyExists(Family family) {
        if (family == null) {
            throw new WebApplicationException(
                    "Família não encontrada.",
                    Response.Status.NOT_FOUND
            );
        }
    }

    private void ensureDependentExists(Dependent dependent) {
        if (dependent == null) {
            throw new WebApplicationException(
                    "Dependente não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }
    }
}