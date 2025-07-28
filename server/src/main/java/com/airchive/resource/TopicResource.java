package com.airchive.resource;

import com.airchive.dto.SessionUser;
import com.airchive.entity.Topic;
import com.airchive.service.TopicService;
import com.airchive.util.SecurityUtils;
import java.util.List;
import javax.annotation.PostConstruct;
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

@Path("/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicResource {

  @Context
  private ServletContext ctx;

  @Context
  private HttpServletRequest request;

  private TopicService topicService;

  @PostConstruct
  public void init() {
    this.topicService = (TopicService) ctx.getAttribute("topicService");
  }

  @GET
  public Response getAllTopics() {
    List<Topic> topics = topicService.getAllTopics();
    return Response.ok(topics).build();
  }

  @GET
  @Path("/search")
  public Response searchTopics(@QueryParam("q") String query) {
    if (query == null || query.isBlank()) {
      return Response.ok(List.of()).build();
    }

    List<Topic> results = topicService.searchTopics(query.trim());
    return Response.ok(results).build();
  }

  @POST
  public Response createTopic(Topic topic) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    Topic created = topicService.createTopic(user, topic);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @PUT
  @Path("/{topicId}")
  public Response updateTopic(@PathParam("topicId") int topicId, Topic topic) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    topicService.updateTopic(user, topicId, topic.code(), topic.fullName());
    return Response.ok().build();
  }

  @DELETE
  @Path("/{topicId}")
  public Response deleteTopic(@PathParam("topicId") int topicId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    topicService.deleteTopic(user, topicId);
    return Response.ok("message", "Topic deleted.").build();
  }
}