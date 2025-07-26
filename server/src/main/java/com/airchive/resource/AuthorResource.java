package com.airchive.resource;

import com.airchive.dto.AuthorCreateRequest;
import com.airchive.dto.AuthorResponse;
import com.airchive.dto.AuthorUpdateRequest;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.PersistenceException;
import com.airchive.exception.ValidationException;
import com.airchive.service.AuthorService;
import com.airchive.util.AuthUtil;
import com.airchive.util.JsonUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorResource {

  @Inject
  private AuthorService authorService;

  @Context
  private HttpServletRequest request;

  /**
   * AUTHOR: Get their own author profile
   */
  @GET
  @Path("/me")
  public Response getMyAuthorProfile() {
    try {
      int userId = AuthUtil.requireAuthorUserId(request);
      AuthorResponse author = authorService.getByUserId(userId);
      return JsonUtil.ok(author);
    } catch (EntityNotFoundException e) {
      return JsonUtil.notFound("Author profile not found.");
    }
  }

  /**
   * AUTHOR: Update their own bio
   */
  @PATCH
  @Path("/me")
  public Response updateBio(AuthorUpdateRequest req) {
    try {
      int userId = AuthUtil.requireAuthorUserId(request);
      AuthorResponse updated = authorService.updateBio(userId, req.bio());
      return JsonUtil.ok(updated);
    } catch (EntityNotFoundException e) {
      return JsonUtil.notFound("Author profile not found.");
    }
  }

  /**
   * ADMIN: List all authors
   */
  @GET
  public Response getAllAuthors() {
    try {
      AuthUtil.requirePermission(request, "ADMIN");
      List<AuthorResponse> authors = authorService.getAll();
      return JsonUtil.ok(authors);
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to fetch authors.");
    }
  }

  /**
   * AUTHOR: Create a new non-platform author (not linked to a user account)
   */
  @POST
  public Response createExternalAuthor(AuthorCreateRequest req) {
    try {
      AuthUtil.requirePermission(request, "AUTHOR");
      AuthorResponse newAuthor = authorService.createNonPlatformAuthor(req);
      return Response.status(Response.Status.CREATED).entity(newAuthor).build();
    } catch (PersistenceException | ValidationException e) {
      return JsonUtil.conflict("Could not create author: " + e.getMessage());
    }
  }

  /**
   * READER: Link their account to an existing author entity
   */
  @POST
  @Path("/link")
  public Response linkToExistingAuthor(@QueryParam("authorId") int authorId) {
    try {
      int userId = AuthUtil.requireSignedInWithPermission(request, "READER");
      AuthorResponse linked = authorService.linkUserToAuthor(authorId, userId);
      return JsonUtil.ok(linked);
    } catch (EntityNotFoundException e) {
      return JsonUtil.notFound("Author not found.");
    } catch (PersistenceException e) {
      return JsonUtil.conflict("Failed to link: " + e.getMessage());
    }
  }
}