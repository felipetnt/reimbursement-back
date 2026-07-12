package com.acme.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.List;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {

        int statusCode = exception.getResponse() != null
                ? exception.getResponse().getStatus()
                : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

        Response.Status status = Response.Status.fromStatusCode(statusCode);

        String error = status != null
                ? status.getReasonPhrase()
                : "Erro";

        String message = exception.getMessage() != null
                ? exception.getMessage()
                : "Erro inesperado.";

        ErrorResponse response = new ErrorResponse(
                statusCode,
                error,
                message,
                List.of(),
                LocalDateTime.now()
        );

        return Response.status(statusCode)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }
}
