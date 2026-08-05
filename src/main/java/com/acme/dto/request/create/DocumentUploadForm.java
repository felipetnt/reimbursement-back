package com.acme.dto.request.create;

import com.acme.domain.enums.DocumentType;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.jboss.resteasy.reactive.RestForm;

import java.util.UUID;

public class DocumentUploadForm {

        @RestForm
        public FileUpload file;

        @RestForm
        public DocumentType documentType;

        @RestForm
        public UUID dependentId;

        @RestForm
        public UUID reimbursementId;
}
