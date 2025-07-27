package com.airchive.resource;

import com.airchive.dto.SessionUser;
import com.airchive.entity.Account;
import com.airchive.entity.AuthorRequest;
import com.airchive.service.AuthorRequestService;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/author-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorRequestResource {

  @Context
  private ServletContext context;

  @Context
  private HttpServletRequest request;

  private AuthorRequestService authorRequestService;

  @PostConstruct
  public void init() {
    this.authorRequestService = (AuthorRequestService) context.getAttribute("authorRequestService");
  }

  private SessionUser getSessionUserOrThrow() {
    HttpSession session = request.getSession(false);
    if (session == null) throw new NotAuthorizedException("Login required.");
    SessionUser user = (SessionUser) session.getAttribute("user");
    if (user == null) throw new NotAuthorizedException("Login required.");
    return user;
  }

  @POST
  public Response submitRequest() {
    SessionUser user = getSessionUserOrThrow();
    if (user.role() != Account.Role.READER) {
      throw new ForbiddenException("Only readers can submit author requests.");
    }

    AuthorRequest created = authorRequestService.submitRequest(user.accountId());
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @GET
  @Path("/pending")
  public Response getPendingRequests(@QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("20") int pageSize) {

    SessionUser user = getSessionUserOrThrow();
    if (!user.isAdmin()) {
      throw new ForbiddenException("Only admins can view pending author requests.");
    }

    List<AuthorRequest> requests = authorRequestService.getPendingRequests(page, pageSize);
    return Response.ok(requests).build();
  }

  @GET
  @Path("/pending/count")
  public Response getPendingCount() {
    SessionUser user = getSessionUserOrThrow();
    if (!user.isAdmin()) {
      throw new ForbiddenException("Only admins can view pending author requests.");
    }

    int count = authorRequestService.countPendingRequests();
    return Response.ok(Map.of("count", count)).build();
  }

  @POST
  @Path("/{accountId}/approve")
  public Response approveRequest(@PathParam("accountId") int accountId) {
    SessionUser user = getSessionUserOrThrow();
    if (!user.isAdmin()) {
      throw new ForbiddenException("Only admins can approve author requests.");
    }

    authorRequestService.approveRequest(accountId);
    return Response.ok(Map.of("message", "Author request approved.")).build();
  }
}