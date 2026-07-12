package com.acme.dto.request.create;
import jakarta.validation.constraints.NotBlank;

public record CreateFamilyRequest (

        @NotBlank(message = "Nome eh obrigatorio")
        String name

){}

