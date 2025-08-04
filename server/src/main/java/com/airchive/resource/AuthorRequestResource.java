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
 * REST API for managing author requests.
 *
 * <p>All endpoints require authentication. Admin-only routes are enforced via service-layer
 * validation via {@link SecurityUtils}.
 *
 * <p>All responses are returned in JSON and use {@link com.airchive.dto} and
 * {@link com.airchive.entity} records.
 */
@Path("/author-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorRequestsResource {

  @Context private ServletContext ctx;
  @Context private HttpServletRequest request;

  private AuthorRequestService getService() {
    return (AuthorRequestService) ctx.getAttribute("authorRequestService");
  }

  /**
   * Submits a new author request for the currently logged-in reader account.
   *
   * @return a {@link Response} containing the newly created {@link AuthorRequest} record
   */
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

  /**
   * Retrieves a paginated list of all pending author requests (admin-only).
   *
   * @param page the page number (default is 1)
   * @param pageSize the number of results per age (default is 20)
   * @return a {@link Response} containing a list of {@link AuthorRequest}s with status {@code
   * PENDING}
   */
  @GET
  @Path("/pending")
  public Response getPendingRequests(
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    List<PendingAuthorRequest> requests = getService().getPendingRequests(user, page, pageSize);
    return Response.ok(requests).build();
  }

  /**
   * Returns the total count of pending author requests (admin-only).
   *
   * @return a {@link Response} containing the count (e.g. {@code {"count": 12}})
   */
  @GET
  @Path("/pending/count")
  public Response getPendingCount() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    int count = getService().countPendingRequests(user);
    return Response.ok(Map.of("count", count)).build();
  }

  /**
   * Approves an existing author request and upgrades the associated account to {@code AUTHOR}.
   *
   * @param accountId the account id of the request to approve
   * @return a {@link Response} containing a confirmation message
   */
  @POST
  @Path("/{accountId}/approve")
  public Response approveRequest(@PathParam("accountId") int accountId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getService().approveRequest(user, accountId);
    return Response.ok(Map.of("message", "Author request approved.")).build();
  }
}