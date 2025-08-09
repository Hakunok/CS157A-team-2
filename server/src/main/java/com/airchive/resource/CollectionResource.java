package com.airchive.resource;

import com.airchive.dto.CollectionResponse;
import com.airchive.dto.CreateOrUpdateCollectionRequest;
import com.airchive.dto.MiniCollection;
import com.airchive.dto.MiniPublication;
import com.airchive.dto.SessionUser;
import com.airchive.service.CollectionService;
import com.airchive.service.PublicationService;
import com.airchive.util.SecurityUtils;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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

/**
 * REST resource for managing user collections and publication saves.
 * <p>
 * This class exposes all endpoints under {@code /collections}. It supports the full collection lifecycle,
 * allowing users to create, update, delete, view, and save publications to collections.
 *
 * <p>
 * <b>Exposed Endpoints:</b>
 * <ul>
 *   <li>{@code POST /collections} - create a new collection for the requesting user</li>
 *   <li>{@code GET /collections/my} - get all collections created by the requesting user</li>
 *   <li>{@code GET /collections/{id}} - get a collection by ID</li>
 *   <li>{@code PUT /collections/{id}} - update a collection owned by the requesting user</li>
 *   <li>{@code DELETE /collections/{id}} - delete a collection owned by the requesting user</li>
 *   <li>{@code GET /collections/recommendations} - get personalized or popular collection recommendations</li>
 *   <li>{@code POST /collections/default/save/{pubId}} - save a publication to the requesting user's default collection</li>
 *   <li>{@code DELETE /collections/default/save/{pubId}} - remove a publication from the requesting user's default collection</li>
 *   <li>{@code GET /collections/default/has/{pubId}} - check if a publication is saved to the requesting user's default collection</li>
 *   <li>{@code GET /collections/default/publications} - list all publications saved to the requesting user's default collection</li>
 *   <li>{@code POST /collections/{id}/add/{pubId}} - add a publication to a requesting user's specific collection</li>
 *   <li>{@code DELETE /collections/{id}/remove/{pubId}} - remove a publication from a requesting user's specific collection</li>
 * </ul>
 *
 * <p>
 * Services are injected manually via {@link ServletContext}, and session authentication
 * is performed using {@link com.airchive.util.SecurityUtils}. All endpoints consume and produce JSON.
 */
@Path("/collections")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CollectionResource {

  // Used to retrieve service instances injected via AppBootstrap
  @Context private ServletContext ctx;

  // Used to extract the current SessionUser for authentication
  @Context private HttpServletRequest request;

  private CollectionService getCollectionService() {
    return (CollectionService) ctx.getAttribute("collectionService");
  }

  private PublicationService getPublicationService() {
    return (PublicationService) ctx.getAttribute("publicationService");
  }

  @POST
  public Response createCollection(CreateOrUpdateCollectionRequest req) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    CollectionResponse created = getCollectionService().createCollection(user, req);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @GET
  @Path("/my")
  public Response getMyCollections() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    List<MiniCollection> collections = getCollectionService().getMyCollections(user);
    return Response.ok(collections).build();
  }

  @GET
  @Path("/{id}")
  public Response getById(@PathParam("id") int collectionId) {
    SessionUser user = SecurityUtils.getSessionUserOrNull(request);
    CollectionResponse response = getCollectionService().getCollectionById(user, collectionId);
    return Response.ok(response).build();
  }

  @GET
  @Path("/recommendations")
  public Response getRecommendations(
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("10") int pageSize
  ) {
    SessionUser user = SecurityUtils.getSessionUserOrNull(request);
    List<MiniCollection> response = getCollectionService().getRecommendedCollections(user, pageSize, page);
    return Response.ok(response).build();
  }

  @PUT
  @Path("/{id}")
  public Response updateCollection(@PathParam("id") int collectionId, CreateOrUpdateCollectionRequest req) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    CollectionResponse edited = getCollectionService().updateCollection(user, collectionId, req);
    return Response.ok(edited).build();
  }

  @DELETE
  @Path("/{id}")
  public Response deleteCollection(@PathParam("id") int collectionId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getCollectionService().deleteCollection(user, collectionId);
    return Response.ok().build();
  }

  @POST
  @Path("/default/save/{pubId}")
  public Response saveToDefault(@PathParam("pubId") int publicationId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getCollectionService().saveToDefaultCollection(user, publicationId);
    return Response.ok().build();
  }

  @DELETE
  @Path("/default/save/{pubId}")
  public Response removeFromDefault(@PathParam("pubId") int publicationId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getCollectionService().removeFromDefaultCollection(user, publicationId);
    return Response.ok().build();
  }

  @GET
  @Path("/default/has/{pubId}")
  public Response isSaved(@PathParam("pubId") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    boolean saved = getCollectionService().isSavedToDefault(user, pubId);
    return Response.ok(Map.of("saved", saved)).build();
  }

  @GET
  @Path("/default/publications")
  public Response listSaved() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    List<MiniPublication> saved = getPublicationService().getPublicationsFromDefault(user);
    return Response.ok(saved).build();
  }

  @POST
  @Path("/{id}/add/{pubId}")
  public Response addToCollection(@PathParam("id") int collectionId, @PathParam("pubId") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getCollectionService().addToCollection(user, collectionId, pubId);
    return Response.ok().build();
  }

  @DELETE
  @Path("/{id}/remove/{pubId}")
  public Response removeFromCollection(@PathParam("id") int collectionId, @PathParam("pubId") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getCollectionService().removeFromCollection(user, collectionId, pubId);
    return Response.ok().build();
  }
}