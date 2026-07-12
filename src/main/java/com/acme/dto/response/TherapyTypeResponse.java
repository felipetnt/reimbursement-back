package com.acme.dto.response;

import java.util.UUID;

public record TherapyTypeResponse(

        UUID id,

        String name,

        UUID dependentId,

        String dependentName

) {
}