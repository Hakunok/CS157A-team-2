package com.airchive.resource;

import com.airchive.dto.CreatePublicationRequest;
import com.airchive.dto.PublicationResponse;
import com.airchive.dto.UpdatePublicationRequest;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.PersistenceException;
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
      return JsonUtil.ok(publicationService.view(pubId));
    } catch (Exception e) {
      return JsonUtil.notFound("Publication not found.");
    }
  }

  /**
   * AUTHOR: Create a new publication
   */
  @POST
  public Response create(CreatePublicationRequest req) {
    try {
      int userId = AuthUtil.requireAuthorUserId(request);
      PublicationResponse pub = publicationService.createPublication(userId, req);
      return JsonUtil.created(pub);
    } catch (ValidationException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to create publication.");
    }
  }

  /**
   * AUTHOR: Update your own submitted publication
   */
  @PATCH
  @Path("/{id}")
  public Response update(@PathParam("id") int pubId, UpdatePublicationRequest req) {
    try {
      int userId = AuthUtil.requireAuthorUserId(request);
      PublicationResponse updated = publicationService.update(pubId, userId, req);
      return JsonUtil.ok(updated);
    } catch (EntityNotFoundException e) {
      return JsonUtil.notFound("Publication not found.");
    } catch (PersistenceException e) {
      return JsonUtil.forbidden(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to update publication.");
    }
  }

  /**
   * AUTHOR: Get all publications where you're listed as an author
   */
  @GET
  @Path("/me")
  public Response getPublicationsByAuthor() {
    try {
      int userId = AuthUtil.requireAuthorUserId(request);
      int authorId = authorService.getAuthorByUserId(userId).id();
      List<PublicationResponse> pubs = publicationService.getByAuthorId(authorId);
      return JsonUtil.ok(pubs);
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to fetch authored publications.");
    }
  }

  /**
   * AUTHOR: Get all publications you submitted
   */
  @GET
  @Path("/submitter")
  public Response getPublicationsISubmitted() {
    try {
      int userId = AuthUtil.requireAuthorUserId(request);
      List<PublicationResponse> pubs = publicationService.getBySubmitterId(userId);
      return JsonUtil.ok(pubs);
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to fetch submitted publications.");
    }
  }

  /**
   * AUTHOR or READER: Like a publication
   */
  @POST
  @Path("/{id}/like")
  public Response like(@PathParam("id") int pubId) {
    try {
      AuthUtil.requirePermission(request, "AUTHOR", "READER");
      publicationService.like(pubId);
      return JsonUtil.ok("Liked.");
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to like publication.");
    }
  }
}
