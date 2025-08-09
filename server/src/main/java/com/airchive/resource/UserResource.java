package com.airchive.resource;

import com.airchive.dto.InteractionSummary;
import com.airchive.dto.SessionUser;
import com.airchive.service.InteractionService;
import com.airchive.service.PersonAccountService;
import com.airchive.util.SecurityUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Map;

/**
 * REST resource for retrieving user-specific and platform-wide usage statistics and interactions.
 * <p>
 * This class provides endpoints for accessing the current user's interaction summaries, usage stats,
 * platform stats, and for triggering affinity recalculations.
 *
 * <p>
 * <b>Exposed Endpoints:</b>
 * <ul>
 *   <li>{@code GET /users/me/stats} - get requesting user's interaction statistics</li>
 *   <li>{@code GET /users/me/interactions} - get the user's most recent publication interactions</li>
 *   <li>{@code GET /users/stats/platform} - get platform-wide statistics</li>
 *   <li>{@code POST /users/me/affinities} - trigger affinity score recalculation for the requesting user</li>
 * </ul>
 *
 * <p>
 * Services are injected manually via {@link ServletContext}, and session authentication
 * is performed using {@link com.airchive.util.SecurityUtils}. All endpoints consume and produce JSON.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

  @Context private ServletContext ctx;
  @Context private HttpServletRequest request;

  private InteractionService getInteractionService() {
    return (InteractionService) ctx.getAttribute("interactionService");
  }

  private PersonAccountService getPersonAccountService() {
    return (PersonAccountService) ctx.getAttribute("personAccountService");
  }

  @GET
  @Path("/me/stats")
  public Response getUserStats() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    Map<String, Integer> stats = getInteractionService().getUserStats(user.accountId());
    return Response.ok(stats).build();
  }

  @GET
  @Path("/me/interactions")
  public Response getRecentInteractions(@QueryParam("limit") @DefaultValue("5") int limit) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    List<InteractionSummary> recent = getInteractionService().getRecentInteractions(user.accountId(), limit);
    return Response.ok(recent).build();
  }

  @GET
  @Path("/stats/platform")
  public Response getPlatformStats() {
    Map<String, Integer> stats = getInteractionService().getPlatformStats();
    return Response.ok(stats).build();
  }

  @POST
  @Path("/me/affinities")
  public Response recalculateAffinities() {
    SessionUser user = SecurityUtils.getSessionUserOrNull(request);
    if (user == null) {
      return Response.ok().entity(null).build();
    }

    getPersonAccountService().updateUserAffinitiesAsync(user.accountId());
    return Response.status(Response.Status.ACCEPTED).build();
  }
}