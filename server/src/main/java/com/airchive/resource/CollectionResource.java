package com.airchive.resource;

import com.airchive.dto.CreateOrUpdateCollectionRequest;
import com.airchive.dto.MiniCollection;
import com.airchive.dto.MiniPublication;
import com.airchive.dto.SessionUser;
import com.airchive.entity.Collection;
import com.airchive.service.CollectionService;
import com.airchive.service.PublicationService;
import com.airchive.util.SecurityUtils;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/collections")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CollectionResource {

  @Context private ServletContext ctx;
  @Context private HttpServletRequest request;

  private CollectionService collectionService;
  private PublicationService publicationService;

  @PostConstruct
  public void init() {
    this.collectionService = (CollectionService) ctx.getAttribute("collectionService");
    this.publicationService = (PublicationService) ctx.getAttribute("publicationService");
  }

  @POST
  public Response createCollection(CreateOrUpdateCollectionRequest req) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    Collection created = collectionService.createCollection(user, req);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @GET
  @Path("/my")
  public Response getMyCollections() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    List<MiniCollection> collections = collectionService.getMyCollections(user);
    return Response.ok(collections).build();
  }

  @PUT
  @Path("/{id}/visibility")
  public Response updateVisibility(@PathParam("id") int collectionId, @QueryParam("public") boolean isPublic) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    collectionService.updateVisibility(user, collectionId, isPublic);
    return Response.ok().build();
  }

  @DELETE
  @Path("/{id}")
  public Response deleteCollection(@PathParam("id") int collectionId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    collectionService.deleteCollection(user, collectionId);
    return Response.ok().build();
  }

  @POST
  @Path("/default/save/{pubId}")
  public Response saveToDefault(@PathParam("pubId") int publicationId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    collectionService.saveToDefaultCollection(user, publicationId);
    return Response.ok().build();
  }

  @DELETE
  @Path("/default/save/{pubId}")
  public Response removeFromDefault(@PathParam("pubId") int publicationId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    collectionService.removeFromDefaultCollection(user, publicationId);
    return Response.ok().build();
  }

  @GET
  @Path("/default/has/{pubId}")
  public Response isSaved(@PathParam("pubId") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    boolean saved = collectionService.isSavedToDefault(user, pubId);
    return Response.ok(Map.of("saved", saved)).build();
  }

  @GET
  @Path("/default/publications")
  public Response listSaved() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    List<MiniPublication> saved = publicationService.getPublicationsFromDefault(user);
    return Response.ok(saved).build();
  }
}