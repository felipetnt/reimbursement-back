package com.acme.mapper;

import com.acme.domain.model.Document;
import com.acme.dto.response.DocumentResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocumentMapper {

    public DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getContentType(),
                document.getFileSize(),
                document.getDocumentType()
        );
    }
}
