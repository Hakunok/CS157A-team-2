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
    return Response.ok(topics).build();
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
    return Response.ok(results).build();
  }

  /**
   * PUBLIC: Get a topic by ID.
   */
  @GET
  @Path("/{id}")
  public Response getById(@PathParam("id") int id) {
    try {
      return Response.ok(topicService.getById(id)).build();
    } catch (Exception e) {
      return JsonUtil.notFound("Topic not found.");
    }
  }

  /**
   * ADMIN: Create a new topic.
   */
  @POST
  public Response createTopic(CreateOrUpdateTopicRequest req) {
    if (!AuthUtil.hasPermission(request, "ADMIN")) {
      return JsonUtil.forbidden("Admin access required.");
    }

    try {
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
    if (!AuthUtil.hasPermission(request, "ADMIN")) {
      return JsonUtil.forbidden("Admin access required.");
    }

    try {
      TopicResponse updated = topicService.updateTopic(topicId, req);
      return Response.ok(updated).build();
    } catch (ValidationException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to update topic.");
    }
  }
}
