package com.airchive.resource;

import com.airchive.dto.SessionUser;
import com.airchive.entity.AuthorRequest;
import com.airchive.service.AuthorRequestService;

import com.airchive.util.SecurityUtils;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/author-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorRequestResource {

  @Context
  private ServletContext ctx;

  @Context
  private HttpServletRequest request;

  private AuthorRequestService authorRequestService;

  @PostConstruct
  public void init() {
    this.authorRequestService = (AuthorRequestService) ctx.getAttribute("authorRequestService");
  }

  @POST
  public Response submitRequest() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    AuthorRequest created = authorRequestService.submitRequest(user);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @GET
  @Path("/pending")
  public Response getPendingRequests(
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    List<AuthorRequest> requests = authorRequestService.getPendingRequests(user, page, pageSize);
    return Response.ok(requests).build();
  }

  @GET
  @Path("/pending/count")
  public Response getPendingCount() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    int count = authorRequestService.countPendingRequests(user);
    return Response.ok(Map.of("count", count)).build();
  }

  @POST
  @Path("/{accountId}/approve")
  public Response approveRequest(@PathParam("accountId") int accountId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    authorRequestService.approveRequest(user, accountId);
    return Response.ok(Map.of("message", "Author request approved.")).build();
  }
}