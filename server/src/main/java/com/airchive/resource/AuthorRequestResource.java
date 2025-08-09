package com.airchive.resource;

import com.airchive.dto.PendingAuthorRequest;
import com.airchive.dto.SessionUser;
import com.airchive.entity.AuthorRequest;
import com.airchive.service.AuthorRequestService;

import com.airchive.util.SecurityUtils;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * REST resource for managing author upgrade requests.
 * <p>
 * This class allows reader accounts to submit requests to become authors, and enables admins to review and
 * approve those requests.
 *
 * <p>
 * <b>Exposed Endpoints:</b>
 * <ul>
 *   <li>{@code POST /author-requests} - submit a new author request for the requesting user (reader only)</li>
 *   <li>{@code GET /author-requests/status} - check if the requesting user has a pending request</li>
 *   <li>{@code GET /author-requests/pending} - get a list of all pending requests (admin only)</li>
 *   <li>{@code GET /author-requests/pending/count} - get the total count of pending requests (admin only)</li>
 *   <li>{@code POST /author-requests/{accountId}/approve} - approve a request and upgrade the account (admin only)</li>
 * </ul>
 *
 * <p>
 * Services are injected manually via {@link ServletContext}, and session authentication
 * is performed using {@link com.airchive.util.SecurityUtils}. All endpoints consume and produce JSON.
 */
@Path("/author-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorRequestResource {

  // Used to retrieve service instances injected via AppBootstrap
  @Context private ServletContext ctx;

  // Used to extract the current SessionUser for authentication
  @Context private HttpServletRequest request;

  private AuthorRequestService getService() {
    return (AuthorRequestService) ctx.getAttribute("authorRequestService");
  }

  @POST
  public Response submitRequest() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    AuthorRequest created = getService().submitRequest(user);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @GET
  @Path("/status")
  public Response getStatus() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);

    boolean pending = getService().hasPendingRequest(user.accountId());

    return Response.ok(Map.of("status", pending ? "PENDING" : "NONE")).build();
  }


  @GET
  @Path("/pending")
  public Response getPendingRequests(
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    List<PendingAuthorRequest> requests = getService().getPendingRequests(user, page, pageSize);
    return Response.ok(requests).build();
  }


  @GET
  @Path("/pending/count")
  public Response getPendingCount() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    int count = getService().countPendingRequests(user);
    return Response.ok(Map.of("count", count)).build();
  }

  @POST
  @Path("/{accountId}/approve")
  public Response approveRequest(@PathParam("accountId") int accountId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getService().approveRequest(user, accountId);
    return Response.ok(Map.of("message", "Author request approved.")).build();
  }
}