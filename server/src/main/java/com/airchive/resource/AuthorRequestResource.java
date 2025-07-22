package com.airchive.resource;

import com.airchive.entity.AuthorRequest;
import com.airchive.exception.DataAccessException;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.PersistenceException;
import com.airchive.service.AuthorRequestService;
import com.airchive.util.AuthUtil;
import com.airchive.util.JsonUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/author-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorRequestResource {

  @Inject
  private AuthorRequestService authorRequestService;

  @Context
  private HttpServletRequest request;

  /**
   * READER: Submit a request to become an author
   */
  @POST
  public Response submitAuthorRequest() {
    if (!AuthUtil.hasPermission(request, "READER")) {
      return JsonUtil.forbidden("Only READERs can submit author requests.");
    }

    try {
      int userId = AuthUtil.getUserId(request);
      AuthorRequest newRequest = authorRequestService.createRequest(userId);
      return Response.status(Response.Status.CREATED).entity(newRequest).build();
    } catch (PersistenceException e) {
      return JsonUtil.conflict("An author request already exists or could not be created.");
    } catch (EntityNotFoundException e) {
      return JsonUtil.notFound("User not found.");
    } catch (DataAccessException e) {
      return JsonUtil.internalError("Database error while creating author request.");
    }
  }

  /**
   * READER: View your own author request
   */
  @GET
  @Path("/me")
  public Response getMyAuthorRequest() {
    if (!AuthUtil.hasPermission(request, "READER")) {
      return JsonUtil.forbidden("Only READERs can view their author request.");
    }

    try {
      int userId = AuthUtil.getUserId(request);
      AuthorRequest req = authorRequestService.getRequestByUserId(userId);
      return Response.ok(req).build();
    } catch (EntityNotFoundException e) {
      return JsonUtil.notFound("No author request found for your account.");
    } catch (DataAccessException e) {
      return JsonUtil.internalError("Database error.");
    }
  }

  /**
   * ADMIN: View all author requests
   */
  @GET
  public Response getAllRequests() {
    if (!AuthUtil.hasPermission(request, "ADMIN")) {
      return JsonUtil.forbidden("Admin access required.");
    }

    try {
      List<AuthorRequest> all = authorRequestService.getPendingRequests();
      return Response.ok(all).build();
    } catch (DataAccessException e) {
      return JsonUtil.internalError("Failed to fetch author requests.");
    }
  }

  /**
   * ADMIN: Approve a specific author request
   */
  @POST
  @Path("/{id}/approve")
  public Response approveRequest(@PathParam("id") int requestId) {
    if (!AuthUtil.hasPermission(request, "ADMIN")) {
      return JsonUtil.forbidden("Admin access required.");
    }

    try {
      AuthorRequest result = authorRequestService.approveRequest(requestId);
      return Response.ok(result).build();
    } catch (EntityNotFoundException e) {
      return JsonUtil.notFound("Author request not found.");
    } catch (PersistenceException e) {
      return JsonUtil.conflict("Failed to approve request: " + e.getMessage());
    } catch (DataAccessException e) {
      return JsonUtil.internalError("Database error.");
    }
  }

  /**
   * ADMIN: Reject a specific author request
   */
  @POST
  @Path("/{id}/reject")
  public Response rejectRequest(@PathParam("id") int requestId) {
    if (!AuthUtil.hasPermission(request, "ADMIN")) {
      return JsonUtil.forbidden("Admin access required.");
    }

    try {
      AuthorRequest result = authorRequestService.rejectRequest(requestId);
      return Response.ok(result).build();
    } catch (EntityNotFoundException e) {
      return JsonUtil.notFound("Author request not found.");
    } catch (PersistenceException e) {
      return JsonUtil.conflict("Failed to reject request: " + e.getMessage());
    } catch (DataAccessException e) {
      return JsonUtil.internalError("Database error.");
    }
  }
}
