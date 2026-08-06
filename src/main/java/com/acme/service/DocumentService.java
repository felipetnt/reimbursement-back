package com.acme.service;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.Document;
import com.acme.domain.model.Reimbursement;
import com.acme.dto.request.create.DocumentUploadForm;
import com.acme.dto.response.DocumentResponse;
import com.acme.mapper.DocumentMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class DocumentService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );

    @Inject
    DocumentMapper mapper;

    @Inject
    FamilyAccessService familyAccessService;

    @Inject
    CurrentUserService currentUserService;

    @Transactional
    public DocumentResponse upload(DocumentUploadForm form){
        currentUserService.requireWritePermission();

        validateForm(form);
        validateFile(form);

        Document document = new Document();
        document.setFileName(form.file.fileName());
        document.setContentType(form.file.contentType());
        document.setFileSize(form.file.size());
        document.setDocumentType(form.documentType);
        document.setFileData(readFileData(form));

        if(form.dependentId != null){
            Dependent dependent = findAccessibleDependent(form.dependentId);
            document.setDependent(dependent);
            document.setReimbursement(null);
        } else {
            Reimbursement reimbursement = findAccessibleReimbursement(form.reimbursementId);
            document.setReimbursement(reimbursement);
            document.setDependent(null);
        }

        document.persist();

        return mapper.toResponse(document);
    }

    public List<DocumentResponse> listByDependent(UUID dependentId){
        Dependent dependent = findAccessibleDependent(dependentId);

        return Document.<Document>list(
                "dependent.id = ?1 order by fileName",
                dependent.getId()
        )
        .stream()
        .map(mapper::toResponse)
        .toList();
    }

    public List<DocumentResponse> listByReimbursement(UUID reimbursementId){
        Reimbursement reimbursement = findAccessibleReimbursement(reimbursementId);

        return Document.<Document>list(
        "reimbursement.id = ?1 order by fileName",
            reimbursement.getId()
        )
        .stream()
        .map(mapper::toResponse)
        .toList();
    }

    public DocumentResponse findById(UUID id){
        return mapper.toResponse(findAccessibleDocument(id));
    }

    public Document getFile(UUID id){
        return findAccessibleDocument(id);
    }

    @Transactional
    public void delete(UUID id){
        currentUserService.requireWritePermission();

        Document document = findAccessibleDocument(id);
        document.delete();
    }

    private void validateForm(DocumentUploadForm form){
        if(form == null){
            throw new WebApplicationException("Os dados do documento são obrigatórios!", Response.Status.BAD_REQUEST);
        }

        if(form.file == null){
            throw new WebApplicationException("O arquivo é obrigatório", Response.Status.BAD_REQUEST);
        }

        if (form.documentType == null) {
            throw new WebApplicationException("O tipo do documento é obrigatório.", Response.Status.BAD_REQUEST);
        }

        boolean hasDependent = form.dependentId != null;
        boolean hasReimbursement = form.reimbursementId != null;

        if(hasDependent == hasReimbursement){
            throw new WebApplicationException("Informe apenas um proprietário: dependente ou reembolso.", Response.Status.BAD_REQUEST);
        }

    }

    private void validateFile(DocumentUploadForm form){

        System.out.println("Nome: " + form.file.fileName());
        System.out.println("Tipo: " + form.file.contentType());
        System.out.println("Tamanho: " + form.file.size());

        if (form.file.fileName() == null || form.file.fileName().isBlank()) {
            throw new WebApplicationException(
                    "O nome do arquivo é inválido.",
                    Response.Status.BAD_REQUEST
            );
        }

        if(form.file.size() <= 0){
            throw new WebApplicationException("O arquivo está vazio", Response.Status.BAD_REQUEST);
        }
        if(form.file.size() > MAX_FILE_SIZE){
            throw new WebApplicationException("O arquivo deve possuir no máximo 10MB", Response.Status.BAD_REQUEST);
        }

        if(form.file.contentType() == null || !ALLOWED_CONTENT_TYPES.contains(form.file.contentType())){
            throw new WebApplicationException("Formato não permitido. Envie um arquivo JPEG, PNG ou PDF", Response.Status.BAD_REQUEST);
        }
    }

    private byte[] readFileData(DocumentUploadForm form){
        try{
            return Files.readAllBytes(form.file.uploadedFile());
        } catch (IOException e){
            throw new WebApplicationException("Não foi possível ler o arquivo enviado", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Document findAccessibleDocument(UUID id) {
        Document document = Document.findById(id);

        if (document == null) {
            throw new WebApplicationException(
                    "Documento não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }

        UUID familyId = getDocumentFamilyId(document);
        familyAccessService.ensureCanAccessFamily(familyId);

        return document;
    }

    private Dependent findAccessibleDependent(UUID id) {
        Dependent dependent = Dependent.findById(id);

        if (dependent == null) {
            throw new WebApplicationException("Dependente não encontrado.", Response.Status.NOT_FOUND
            );
        }

        familyAccessService.ensureCanAccessFamily(dependent.getFamily().getId());

        return dependent;
    }

    private Reimbursement findAccessibleReimbursement(UUID id) {
        Reimbursement reimbursement = Reimbursement.findById(id);

        if (reimbursement == null) {
            throw new WebApplicationException("Reembolso não encontrado.", Response.Status.NOT_FOUND);
        }

        UUID familyId = reimbursement
                .getTherapy()
                .getDependent()
                .getFamily()
                .getId();

        familyAccessService.ensureCanAccessFamily(familyId);

        return reimbursement;
    }

    public UUID getDocumentFamilyId(Document document){
        if(document.getDependent() != null){
            return document.getDependent().getFamily().getId();
        } else {
            return document
                    .getReimbursement()
                    .getTherapy()
                    .getDependent()
                    .getFamily()
                    .getId();
        }
    }
}
