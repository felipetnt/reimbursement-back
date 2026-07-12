package com.acme.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record DependentResponse(

        UUID id,

        String name,

        LocalDate birthDate,

        UUID familyId,

        String familyName

) {}
