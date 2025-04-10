package org.apikey.controller;

import org.apikey.model.ApiKey;
import org.apikey.service.ApiKeyService;
import org.apikey.entity.ApiKeyEntity;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;


@Path("/apikey")
public class ApiKeyController {
    private static final Logger LOG = Logger.getLogger(ApiKeyController.class);

    @Inject
    ApiKeyService apiKeyService;

    @GET
    @Path("/{clientId}/platform/{plataform}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getApiKey(@PathParam("clientId") String clientId, @PathParam("plataform") String plataform) {
        LOG.debug("Inicio del método getApiKey");
        LOG.debug("clientId:"+clientId);
        LOG.debug("plataform:"+plataform);
        return Response.ok(apiKeyService.getApiKeyClient(clientId, plataform)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postApiKey(final ApiKey apikey) {
        System.out.println(apikey);
        return Response.ok(apiKeyService.createApiKey(apikey)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putApiKey(final ApiKey apikey) {
        return Response.ok(apiKeyService.updateApiKey(apikey)).build();
    }

    @PATCH
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pathApiKey(@PathParam("id") String id, final ApiKey apikey) {

        return Response.ok(apiKeyService.patchApiKey(id, apikey)).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getApiKeyById(@PathParam("id") String id) {
        ApiKeyEntity apiKey = apiKeyService.getApiKeyById(id);
        return Response.ok(apiKey).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteApiKey(@PathParam("id") String id) {
        apiKeyService.deleteApiKey(id);
        return Response.ok().build();
    }

    @PUT
    @Path("/refresh")
    @Operation(
            summary = "Regenerar API key",
            description = "Genera una nueva API key y extiende su validez por 24 horas desde la zona horaria de México"
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "API key regenerada exitosamente con nuevo valor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiKeyRefreshDto.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "API key no encontrada, inactiva o expirada"
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Header de autorización inválido"
            )
    })

    @PUT
    @Path("/refresh/seller/{sellerId}")
    @Operation(
            summary = "Regenerar API key por Seller ID",
            description = "Genera una nueva API key para el Seller ID especificado"
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "API key regenerada exitosamente con nuevo valor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiKeyRefreshDto.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "No se encontró una API key activa para el Seller ID proporcionado"
            )
    })
    public Response refreshApiKeyBySellerId(
            @PathParam("sellerId")
            @Parameter(description = "ID del vendedor", required = true)
            String sellerId) {

        ApiKeyRefreshDto refreshResult = apiKeyService.refreshApiKeyBySellerIdAndGetInfo(sellerId);

        if (!refreshResult.getSuccess()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(refreshResult)
                    .build();
        }

        return Response.ok(refreshResult).build();
    }
}
