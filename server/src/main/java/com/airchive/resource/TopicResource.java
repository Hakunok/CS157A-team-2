package com.airchive.resource;

import com.airchive.dto.SessionUser;
import com.airchive.entity.Topic;
import com.airchive.service.TopicService;
import com.airchive.util.SecurityUtils;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
 * REST resource for managing publication topics.
 * <p>
 * This class exposes endpoints allowing users to view and search topics, and admins to create, update,
 * or delete them.
 *
 * <p>
 * <b>Exposed Endpoints:</b>
 * <ul>
 *   <li>{@code GET /topics} - list all topics</li>
 *   <li>{@code GET /topics/search?q=...} - search topics by name or code</li>
 *   <li>{@code POST /topics} - create a new topic (admin only)</li>
 *   <li>{@code PUT /topics/{topicId}} - update an existing topic (admin only)</li>
 *   <li>{@code DELETE /topics/{topicId}} - delete a topic (admin only)</li>
 * </ul>
 *
 * <p>
 * Services are injected manually via {@link ServletContext}, and session authentication
 * is performed using {@link com.airchive.util.SecurityUtils}. All endpoints consume and produce JSON.
 */
@Path("/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicResource {

  // Used to retrieve service instances injected via AppBootstrap
  @Context private ServletContext ctx;

  // Used to extract the current SessionUser for authentication
  @Context private HttpServletRequest request;

  private TopicService getService() {
    return (TopicService) ctx.getAttribute("topicService");
  }


  @GET
  public Response getAllTopics() {
    List<Topic> topics = getService().getAllTopics();
    return Response.ok(topics).build();
  }


  @GET
  @Path("/search")
  public Response searchTopics(@QueryParam("q") String query) {
    if (query == null || query.isBlank()) {
      return Response.ok(List.of()).build();
    }

    List<Topic> results = getService().searchTopics(query.trim());
    return Response.ok(results).build();
  }


  @POST
  public Response createTopic(Topic topic) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    Topic created = getService().createTopic(user, topic);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }


  @PUT
  @Path("/{topicId}")
  public Response updateTopic(@PathParam("topicId") int topicId, Topic topic) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getService().updateTopic(user, topicId, topic.code(), topic.fullName());
    return Response.ok().build();
  }


  @DELETE
  @Path("/{topicId}")
  public Response deleteTopic(@PathParam("topicId") int topicId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getService().deleteTopic(user, topicId);
    return Response.ok(Map.of("message", "Topic deleted.")).build();
  }
}