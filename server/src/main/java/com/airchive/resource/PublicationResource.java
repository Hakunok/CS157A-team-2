package com.airchive.resource;

import com.airchive.dto.Draft;
import com.airchive.dto.MiniPublication;
import com.airchive.dto.PublicationResponse;
import com.airchive.dto.PublishRequest;
import com.airchive.dto.SessionUser;
import com.airchive.entity.Person;
import com.airchive.entity.Publication;
import com.airchive.service.PersonAccountService;
import com.airchive.service.PublicationService;
import com.airchive.util.SecurityUtils;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
 * REST resource for managing publications, drafts, publishing, recommendations, and user interactions
 * such as likes and views.
 * <p>
 * This class exposes all endpoints under {@code /publications}. It supports the full publication
 * lifecycle from draft creation to publication. It also includes endpoints for personalized recommendations
 * and user interaction tracking.
 *
 * <p>
 * <b>Endpoints:</b>
 * <ul>
 *   <li>{@code POST /publications} - create a new draft for the requesting user</li>
 *   <li>{@code PUT /publications/{id}} - update a draft owned by the requesting user</li>
 *   <li>{@code POST /publications/{id}/publish} - publish a draft owned by the requesting user</li>
 *   <li>{@code GET /publications/{id}} - get a publication by id</li>
 *   <li>{@code GET /publications/my} - get all publications created by the requesting user</li>
 *   <li>{@code GET /publications/search} - search for publications by title</li>
 *   <li>{@code GET /publications/recommendations} - get personalized or popular publication recommendations</li>
 *   <li>{@code POST /publications/{id}/like} - like a publication for the requesting user</li>
 *   <li>{@code DELETE /publications/{id}/like} - unlike a publication for the requesting user</li>
 *   <li>{@code GET /publications/{id}/like} - check if a publication is liked by the requesting user</li>
 *   <li>{@code POST /publications/{id}/view} - register a view interaction for the requesting user</li>
 *   <li>{@code GET /publications/person-by-email/{email}} - search for a person by email</li>
 *   <li>{@code POST /publications/create-author} - create a new person/author</li>
 * </ul>
 *
 * <p>
 * Services are injected manually via {@link ServletContext}, and session authentication
 * is performed using {@link com.airchive.util.SecurityUtils}. All endpoints consume and produce JSON.
 */
@Path("/publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicationResource {

  // Used to retrieve service instances injected via AppBootstrap
  @Context private ServletContext ctx;

  // Used to extract the current SessionUser for authentication
  @Context private HttpServletRequest request;

  private PublicationService getPublicationService() {
    return (PublicationService) ctx.getAttribute("publicationService");
  }

  private PersonAccountService getPersonService() {
    return (PersonAccountService) ctx.getAttribute("personAccountService");
  }

  @POST
  public Response createDraft(Draft draft) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    PublicationResponse created = getPublicationService().createDraft(user, draft);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }


  @PUT
  @Path("/{id}")
  public Response editDraft(@PathParam("id") int pubId, Draft draft) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    PublicationResponse updated = getPublicationService().editDraft(user, pubId, draft);
    return Response.ok(updated).build();
  }


  @POST
  @Path("/{id}/publish")
  public Response publish(@PathParam("id") int pubId, PublishRequest publishRequest) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    PublicationResponse published = getPublicationService().publishDraft(user, pubId, publishRequest);
    return Response.ok(published).build();
  }


  @GET
  @Path("/{id}")
  public Response getById(@PathParam("id") int pubId) {
    PublicationResponse response = getPublicationService().getPublicationById(pubId);
    return Response.ok(response).build();
  }


  @GET
  @Path("/search")
  public Response search(@QueryParam("q") String query) {
    List<MiniPublication> results = getPublicationService().searchByTitle(query);
    return Response.ok(results).build();
  }


  @GET
  @Path("/my")
  public Response getMyPublications() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    List<MiniPublication> mine = getPublicationService().getMyPublications(user);
    return Response.ok(mine).build();
  }


  @GET
  @Path("/recommendations")
  public Response recommendations(
      @QueryParam("kind") String kindStr,
      @QueryParam("kinds") List<String> kindListStr,
      @QueryParam("topicId") List<Integer> topicIds,
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("10") int pageSize
  ) {
    List<Publication.Kind> kinds = null;

    if (kindStr != null && !kindStr.isBlank()) {
      kinds = List.of(Publication.Kind.valueOf(kindStr.toUpperCase()));
    } else if (kindListStr != null && !kindListStr.isEmpty()) {
      kinds = kindListStr.stream().map(s -> Publication.Kind.valueOf(s.toUpperCase())).toList();
    }

    SessionUser user = SecurityUtils.getSessionUserOrNull(request);

    if (topicIds != null && !topicIds.isEmpty()) {
      List<MiniPublication> results = getPublicationService().getByTopicsAndKinds(topicIds, kinds, page, pageSize, user);
      return Response.ok(results).build();
    }

    List<MiniPublication> recs = getPublicationService().getRecommendations(user, kinds, page, pageSize);
    return Response.ok(recs).build();
  }


  @POST
  @Path("/{id}/like")
  public Response like(@PathParam("id") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getPublicationService().likePublication(user, pubId);
    return Response.ok().build();
  }


  @DELETE
  @Path("/{id}/like")
  public Response unlike(@PathParam("id") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getPublicationService().unlikePublication(user, pubId);
    return Response.ok().build();
  }


  @GET
  @Path("/{id}/like")
  public Response hasLiked(@PathParam("id") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    boolean liked = getPublicationService().hasLikedPublication(user, pubId);
    return Response.ok(Map.of("liked", liked)).build();
  }


  @POST
  @Path("/{id}/view")
  public Response view(@PathParam("id") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrNull(request);
    if (user != null) {
      getPublicationService().viewPublication(user, pubId);
    }
    return Response.ok().build();
  }

  @GET
  @Path("/person-by-email/{email}")
  public Response getPersonByEmail(@PathParam("email") String email) {
    PersonAccountService personService = getPersonService();
    Person person = personService.getPersonByEmail(email);
    return Response.ok(person).build();
  }

  @POST
  @Path("/create-author")
  public Response createPerson(Person person) {
    PersonAccountService personService = getPersonService();
    Person created = personService.createPerson(person);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }
}