package com.airchive.resource;

import com.airchive.dto.CreateOrUpdateTopicRequest;
import com.airchive.dto.TopicResponse;
import com.airchive.exception.ValidationException;
import com.airchive.service.TopicService;
import com.airchive.util.AuthUtil;
import com.airchive.util.JsonUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicResource {

  @Inject
  private TopicService topicService;

  @Context
  private HttpServletRequest request;

  /**
   * PUBLIC: Get all topics.
   */
  @GET
  public Response getAllTopics() {
    List<TopicResponse> topics = topicService.getAllTopics();
    return JsonUtil.ok(topics);
  }

  /**
   * PUBLIC: Search topics by query string (name/code).
   */
  @GET
  @Path("/search")
  public Response search(@QueryParam("q") String query) {
    if (query == null || query.isBlank()) {
      return JsonUtil.badRequest("Query must not be empty.");
    }

    List<TopicResponse> results = topicService.search(query);
    return JsonUtil.ok(results);
  }

  /**
   * PUBLIC: Get a topic by ID.
   */
  @GET
  @Path("/{id}")
  public Response getById(@PathParam("id") int id) {
    try {
      TopicResponse topic = topicService.getById(id);
      return JsonUtil.ok(topic);
    } catch (Exception e) {
      return JsonUtil.notFound("Topic not found.");
    }
  }

  /**
   * ADMIN: Create a new topic.
   */
  @POST
  public Response createTopic(CreateOrUpdateTopicRequest req) {
    try {
      AuthUtil.requirePermission(request, "ADMIN");

      TopicResponse topic = topicService.createTopic(req);
      return Response.status(Response.Status.CREATED).entity(topic).build();

    } catch (ValidationException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to create topic.");
    }
  }

  /**
   * ADMIN: Update an existing topic.
   */
  @PUT
  @Path("/{id}")
  public Response updateTopic(@PathParam("id") int topicId, CreateOrUpdateTopicRequest req) {
    try {
      AuthUtil.requirePermission(request, "ADMIN");

      TopicResponse updated = topicService.updateTopic(topicId, req);
      return JsonUtil.ok(updated);

    } catch (ValidationException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to update topic.");
    }
  }
}
