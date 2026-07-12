package com.acme.service;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.TherapyType;
import com.acme.dto.request.create.CreateTherapyTypeRequest;
import com.acme.dto.request.update.UpdateTherapyTypeRequest;
import com.acme.dto.response.TherapyTypeResponse;
import com.acme.mapper.TherapyTypeMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TherapyTypeService {

    @Inject
    TherapyTypeMapper mapper;

    public List<TherapyTypeResponse> list() {
        return TherapyType.<TherapyType>listAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public TherapyTypeResponse findById(UUID id) {
        TherapyType therapyType = TherapyType.findById(id);

        ensureTherapyTypeExists(therapyType);

        return mapper.toResponse(therapyType);
    }

    public List<TherapyTypeResponse> listByDependent(UUID dependentId) {
        Dependent dependent = Dependent.findById(dependentId);

        ensureDependentExists(dependent);

        return TherapyType.<TherapyType>list("dependent.id", dependentId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TherapyTypeResponse create(CreateTherapyTypeRequest request) {
        Dependent dependent = Dependent.findById(request.dependentId());

        ensureDependentExists(dependent);

        ensureNameAvailable(request.name(), request.dependentId());

        TherapyType therapyType = mapper.toEntity(request, dependent);

        therapyType.persist();

        return mapper.toResponse(therapyType);
    }

    @Transactional
    public TherapyTypeResponse update(UUID id, UpdateTherapyTypeRequest request) {
        TherapyType therapyTypeBeforeUpdate = TherapyType.findById(id);

        ensureTherapyTypeExists(therapyTypeBeforeUpdate);

        Dependent dependent = Dependent.findById(request.dependentId());

        ensureDependentExists(dependent);

        ensureNameAvailableForUpdate(request.name(), request.dependentId(), id);

        TherapyType therapyType = mapper.updateEntity(
                request,
                therapyTypeBeforeUpdate,
                dependent
        );

        return mapper.toResponse(therapyType);
    }

    @Transactional
    public void delete(UUID id) {
        TherapyType therapyType = TherapyType.findById(id);

        ensureTherapyTypeExists(therapyType);

        therapyType.delete();
    }

    private void ensureDependentExists(Dependent dependent) {
        if (dependent == null) {
            throw new WebApplicationException(
                    "Dependente não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }
    }

    private void ensureTherapyTypeExists(TherapyType therapyType) {
        if (therapyType == null) {
            throw new WebApplicationException(
                    "Tipo de terapia não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }
    }

    private void ensureNameAvailable(String name, UUID dependentId) {
        long count = TherapyType.count(
                "lower(name) = lower(?1) and dependent.id = ?2",
                name.trim(),
                dependentId
        );

        if (count > 0) {
            throw new WebApplicationException(
                    "Já existe um tipo de terapia com este nome para este dependente.",
                    Response.Status.CONFLICT
            );
        }
    }

    private void ensureNameAvailableForUpdate(String name,
                                              UUID dependentId,
                                              UUID therapyTypeId) {
        TherapyType existing = TherapyType.find(
                "lower(name) = lower(?1) and dependent.id = ?2",
                name.trim(),
                dependentId
        ).firstResult();

        if (existing != null && !existing.getId().equals(therapyTypeId)) {
            throw new WebApplicationException(
                    "Já existe um tipo de terapia com este nome para este dependente.",
                    Response.Status.CONFLICT
            );
        }
    }
}