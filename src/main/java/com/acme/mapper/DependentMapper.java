package com.acme.mapper;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.Family;
import com.acme.dto.request.create.CreateDependentRequest;
import com.acme.dto.request.update.UpdateDependentRequest;
import com.acme.dto.response.DependentResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DependentMapper {

    public Dependent toEntity(CreateDependentRequest request, Family family) {

        Dependent dependent = new Dependent();

        dependent.setName(request.name());
        dependent.setBirthDate(request.birthDate());
        dependent.setFamily(family);

        return dependent;
    }

    public Dependent updateEntity(UpdateDependentRequest request, Dependent dependent, Family family) {

        dependent.setName(request.name());
        dependent.setBirthDate(request.birthDate());
        dependent.setFamily(family);

        return dependent;
    }

    public DependentResponse toResponse(Dependent dependent) {

        return new DependentResponse(
                dependent.getId(),
                dependent.getName(),
                dependent.getBirthDate(),
                dependent.getFamily().getId(),
                dependent.getFamily().getName()
        );
    }
}