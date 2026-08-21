package com.acme.resource;

import com.acme.dto.response.DashboardResponse;
import com.acme.service.DashboardService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/dashboard")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Dashboard", description = "Indicadores gerais de reembolsos")
public class DashboardResource {

    @Inject
    DashboardService service;

    @GET
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(
            summary = "Obtém os indicadores do dashboard",
            description = "ADMIN pode visualizar todas as famílias ou filtrar por família. USER e VIEWER visualizam somente a própria família."
    )
    @APIResponse(responseCode = "200", description = "Indicadores encontrados")
    public DashboardResponse getDashboard(@QueryParam("familyId") UUID familyId) {
        return service.getDashboard(familyId);
    }
}