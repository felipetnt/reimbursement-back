package com.acme.resource;

import com.acme.domain.model.Document;
import com.acme.domain.model.Reimbursement;
import com.acme.dto.request.create.DocumentUploadForm;
import com.acme.dto.response.DocumentResponse;
import com.acme.dto.response.ReimbursementResponse;
import com.acme.service.DocumentService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;


@Path("/documents")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Documents", description = "Gerenciamento de imagens e documentos")
public class DocumentResource {

    @Inject
    DocumentService service;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Envia uma imagem ou documento")
    @APIResponse(responseCode = "201", description = "Documento enviado com sucesso")
    @APIResponse(responseCode = "400", description = "Arquivo ou vínculo inválido")
    @APIResponse(responseCode = "403", description = "Usuário sem acesso à família")
    @APIResponse(responseCode = "404", description = "Dependente ou reembolso não encontrado")
    public Response upload(@BeanParam DocumentUploadForm form){
        DocumentResponse response = service.upload(form);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Obtém dados de um documento")
    @APIResponse(responseCode = "200", description = "Documento encontrado")
    @APIResponse(responseCode = "404", description = "Documento não encontrado")
    public DocumentResponse findById(@PathParam("id") UUID id){
        return service.findById(id);
    }

    @GET
    @Path("/dependent/{dependentId}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista os documentos globais de um dependente")
    @APIResponse(responseCode = "200", description = "Documentos encontrados")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    public List<DocumentResponse> listByDependent(@PathParam("dependentId") UUID dependentId){
        return service.listByDependent(dependentId);
    }

    @GET
    @Path("/reimbursement/{reimbursementId}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista os documentos relacionados ao reembolso")
    @APIResponse(responseCode = "200", description = "Documentos encontrados")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    public List<DocumentResponse> listByReimbursement(@PathParam("reimbursementId") UUID reimbursementId){
        return service.listByReimbursement(reimbursementId);
    }

    @GET
    @Path("/{id}/view")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Produces(MediaType.WILDCARD)
    @Operation(summary = "Visualiza um documento")
    public Response view(@PathParam("id") UUID id) {
        Document document = service.getFile(id);

        return buildFileResponse(document, "inline");
    }

    @GET
    @Path("/{id}/download")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Produces(MediaType.WILDCARD)
    @Operation(summary = "Baixa um documento")
    public Response download(@PathParam("id") UUID id) {
        Document document = service.getFile(id);

        return buildFileResponse(document, "attachment");
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Remove um documento")
    @APIResponse(responseCode = "204", description = "Documento removido com sucesso")
    @APIResponse(responseCode = "404", description = "Documento não encontrado")
    public Response delete(@PathParam("id") UUID id){
        service.delete(id);
        return Response.noContent().build();
    }

    private Response buildFileResponse(Document document, String disposition) {
        String fileName = sanitizeFileName(document.getFileName());

        return Response.ok(document.getFileData())
                .type(document.getContentType())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition + "; filename=\"" + fileName + "\""
                )
                .header(HttpHeaders.CONTENT_LENGTH, document.getFileSize())
                .build();
    }

    private String sanitizeFileName(String fileName) {
        return fileName
                .replace("\"", "")
                .replace("\r", "")
                .replace("\n", "");
    }
}
