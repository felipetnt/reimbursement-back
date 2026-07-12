package com.acme.mapper;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.TherapyType;
import com.acme.dto.request.create.CreateTherapyTypeRequest;
import com.acme.dto.request.update.UpdateTherapyTypeRequest;
import com.acme.dto.response.TherapyTypeResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TherapyTypeMapper {

    public TherapyType toEntity(CreateTherapyTypeRequest request,
                                Dependent dependent) {

        TherapyType therapyType = new TherapyType();

        therapyType.setName(request.name().trim());
        therapyType.setDependent(dependent);

        return therapyType;
    }

    public TherapyType updateEntity(UpdateTherapyTypeRequest request,
                                    TherapyType therapyType,
                                    Dependent dependent) {

        therapyType.setName(request.name().trim());
        therapyType.setDependent(dependent);

        return therapyType;
    }

    public TherapyTypeResponse toResponse(TherapyType therapyType) {

        return new TherapyTypeResponse(
                therapyType.getId(),
                therapyType.getName(),
                therapyType.getDependent().getId(),
                therapyType.getDependent().getName()
        );
    }
}