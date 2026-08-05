package com.acme.dto.response;

import com.acme.domain.enums.DocumentType;

import java.util.UUID;

public record DocumentResponse(
    UUID id,
    String fileName,
    String contentType,
    Long fileSize,
    DocumentType documentType
){}
