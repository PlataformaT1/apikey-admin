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

@Path("/apikey")
public class ApiKeyController {

    @Inject
    ApiKeyService apiKeyService;

    @GET
    @Path("/{clientId}/platform/{plataform}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getApiKey(@PathParam("clientId") String clientId, @PathParam("plataform") String plataform) {

        return Response.ok(apiKeyService.getApiKeyClient(clientId, plataform)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postApiKey(final ApiKey apikey) {

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
}
