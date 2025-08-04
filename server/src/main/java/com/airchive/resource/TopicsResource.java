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
 * REST API for managing publication topics.
 *
 * <p>This resource allows all users to view and search topics, while only administrators are
 * allowed to create, update, or delete them.
 *
 * <p>All responses are returned in JSON and may use {@link com.airchive.dto} and
 * {@link com.airchive.entity} records.
 */
@Path("/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicsResource {

  @Context private ServletContext ctx;
  @Context private HttpServletRequest request;

  private TopicService getService() {
    return (TopicService) ctx.getAttribute("topicService");
  }

  /**
   * Retrieves all topics available in the system.
   *
   * @return a list of all {@link Topic} records
   */
  @GET
  public Response getAllTopics() {
    List<Topic> topics = getService().getAllTopics();
    return Response.ok(topics).build();
  }

  /**
   * Searches for topics by keyword in their full name or code.
   *
   * @param query the search keyword
   * @return a list of matching {@link Topic}s, or an empty list if none found
   */
  @GET
  @Path("/search")
  public Response searchTopics(@QueryParam("q") String query) {
    if (query == null || query.isBlank()) {
      return Response.ok(List.of()).build();
    }

    List<Topic> results = getService().searchTopics(query.trim());
    return Response.ok(results).build();
  }

  /**
   * Creates a new topic (admin-only).
   *
   * @param topic the topic data to create
   * @return the created {@link Topic}
   */
  @POST
  public Response createTopic(Topic topic) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    Topic created = getService().createTopic(user, topic);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  /**
   * Updates the code and full name of an existing topic (admin-only).
   *
   * @param topicId the id of the topic to update
   * @param topic the updated topic data
   * @return 200 OK
   */
  @PUT
  @Path("/{topicId}")
  public Response updateTopic(@PathParam("topicId") int topicId, Topic topic) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getService().updateTopic(user, topicId, topic.code(), topic.fullName());
    return Response.ok().build();
  }

  /**
   * Deletes a topic by its id (admin-only).
   *
   * @param topicId the id of the topic to delete
   * @return a confirmation message
   */
  @DELETE
  @Path("/{topicId}")
  public Response deleteTopic(@PathParam("topicId") int topicId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getService().deleteTopic(user, topicId);
    return Response.ok(Map.of("message", "Topic deleted.")).build();
  }
}