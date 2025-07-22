package com.airchive.resource;

import com.airchive.dto.CreatePublicationRequest;
import com.airchive.dto.PublicationResponse;
import com.airchive.dto.UpdatePublicationRequest;
import com.airchive.exception.ValidationException;
import com.airchive.service.AuthorService;
import com.airchive.service.PublicationService;
import com.airchive.util.AuthUtil;
import com.airchive.util.JsonUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicationResource {

  @Inject
  private PublicationService publicationService;

  @Inject
  private AuthorService authorService;

  @Context
  private HttpServletRequest request;

  /**
   * PUBLIC: View all published publications (paginated)
   */
  @GET
  public Response getAll(@QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("size") @DefaultValue("20") int size) {
    List<PublicationResponse> result = publicationService.getAll(page, size);
    return JsonUtil.ok(result);
  }

  /**
   * PUBLIC: View a specific publication (increments view count)
   */
  @GET
  @Path("/{id}")
  public Response getById(@PathParam("id") int pubId) {
    try {
      return Response.ok(publicationService.view(pubId)).build();
    } catch (Exception e) {
      return JsonUtil.notFound("Publication not found.");
    }
  }

  /**
   * AUTHOR: Create a new publication
   */
  @POST
  public Response create(CreatePublicationRequest req) {
    if (!AuthUtil.hasPermission(request, "AUTHOR")) {
      return JsonUtil.forbidden("Only authors can create publications.");
    }

    try {
      int userId = AuthUtil.getUserId(request);
      PublicationResponse pub = publicationService.createPublication(userId, req);
      return JsonUtil.created(pub);
    } catch (ValidationException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to create publication.");
    }
  }

  /**
   * AUTHOR: Update your own publication
   */
  @PUT
  @Path("/{id}")
  public Response update(@PathParam("id") int pubId, UpdatePublicationRequest req) {
    if (!AuthUtil.hasPermission(request, "AUTHOR")) {
      return JsonUtil.forbidden("Only authors can update publications.");
    }

    try {
      int userId = AuthUtil.getUserId(request);
      PublicationResponse updated = publicationService.update(pubId, userId, req);
      return JsonUtil.ok(updated);
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to update publication.");
    }
  }

  /**
   * AUTHOR: Get all your authored publications
   */
  @GET
  @Path("/me")
  public Response getMyPublications() {
    if (!AuthUtil.hasPermission(request, "AUTHOR")) {
      return JsonUtil.forbidden("Only authors can access this.");
    }

    try {
      int userId = AuthUtil.getUserId(request);
      int authorId = authorService.getAuthorByUserId(userId).id();
      List<PublicationResponse> pubs = publicationService.getByAuthorId(authorId);
      return JsonUtil.ok(pubs);
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to fetch authored publications.");
    }
  }

  /**
   * AUTHOR or READER: Like a publication (increments like count)
   */
  @POST
  @Path("/{id}/like")
  public Response like(@PathParam("id") int pubId) {
    if (!AuthUtil.hasPermission(request, "AUTHOR", "READER")) {
      return JsonUtil.forbidden("Only logged-in users can like publications.");
    }

    try {
      publicationService.like(pubId);
      return JsonUtil.ok("Liked.");
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to like publication.");
    }
  }
}
