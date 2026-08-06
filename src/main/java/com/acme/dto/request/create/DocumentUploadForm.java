package com.acme.dto.request.create;

import com.acme.domain.enums.DocumentType;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.RestForm;

import java.util.UUID;

public class DocumentUploadForm {

        @RestForm
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public FileUpload file;

        @RestForm
        public DocumentType documentType;

        @RestForm
        public UUID dependentId;

        @RestForm
        public UUID reimbursementId;
}
