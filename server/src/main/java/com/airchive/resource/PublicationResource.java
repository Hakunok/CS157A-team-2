package com.airchive.resource;

import com.airchive.dto.Draft;
import com.airchive.dto.MiniPublication;
import com.airchive.dto.PublicationResponse;
import com.airchive.dto.PublishRequest;
import com.airchive.dto.SessionUser;
import com.airchive.entity.Publication;
import com.airchive.service.PublicationService;
import com.airchive.util.SecurityUtils;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
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
 * REST API for managing publications including drafts, publishing, recommendations, and user
 * interactions such as likes and views.
 *
 * <p>Some endpoints require authentication. Role-based routes are enforced through service-layer
 * validation via {@link SecurityUtils}.
 *
 * <p>All responses are returned in JSON and use {@link com.airchive.dto} and
 * {@link com.airchive.entity} records.
 */
@Path("/publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicationResource {

  @Context private ServletContext ctx;
  @Context private HttpServletRequest request;

  private PublicationService publicationService;

  @PostConstruct
  public void init() {
    publicationService = (PublicationService) ctx.getAttribute("publicationService");
  }

  /**
   * Creates a new draft publication.
   *
   * @param draft the draft metadata to save
   * @return the created {@link PublicationResponse}
   */
  @POST
  public Response createDraft(Draft draft) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    PublicationResponse created = publicationService.createDraft(user, draft);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  /**
   * Updates the content of an existing draft.
   *
   * @param pubId the id of the draft to update
   * @param draft the updated draft content
   * @return the updated {@link PublicationResponse}
   */
  @PUT
  @Path("/{id}")
  public Response editDraft(@PathParam("id") int pubId, Draft draft) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    PublicationResponse updated = publicationService.editDraft(user, pubId, draft);
    return Response.ok(updated).build();
  }

  /**
   * Publishes a finalized draft by attaching authors, topics, and optionally setting a published
   * date.
   *
   * @param pubId the id of the draft to publish
   * @param publishRequest the author ids, topic ids, and optional publish time
   * @return the finalized {@link PublicationResponse}
   */
  @POST
  @Path("/{id}/publish")
  public Response publish(@PathParam("id") int pubId, PublishRequest publishRequest) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    PublicationResponse published = publicationService.publishDraft(user, pubId, publishRequest);
    return Response.ok(published).build();
  }

  /**
   * Retrieves full comprehensive publication metadata by id.
   *
   * @param pubId the id of the publication
   * @return the comprehensive {@link PublicationResponse}
   */
  @GET
  @Path("/{id}")
  public Response getById(@PathParam("id") int pubId) {
    PublicationResponse response = publicationService.getPublicationById(pubId);
    return Response.ok(response).build();
  }

  /**
   * Searches for published publications by title with a fuzzyish full-text match.
   * @param query the search keywords
   * @return a list of {@link MiniPublication} responses
   */
  @GET
  @Path("/search")
  public Response search(@QueryParam("q") String query) {
    List<MiniPublication> results = publicationService.searchByTitle(query);
    return Response.ok(results).build();
  }

  /**
   * Lists all publications submitted by the currently authenticated author.
   *
   * @return a list of {@link MiniPublication}s created by the user
   */
  @GET
  @Path("/my")
  public Response getMyPublications() {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    List<MiniPublication> mine = publicationService.getMyPublications(user);
    return Response.ok(mine).build();
  }

  /**
   * Returns personalized publication recommendations based on interaction history and topic and
   * author affinities.
   *
   * @param page the current page of results
   * @param pageSize the number of results per page
   * @param kind an optional filter for publication kind (e.g., BLOG, PAPER)
   * @return a list of recommended {@link MiniPublication}s
   */
  @GET
  @Path("/recommendations")
  public Response recommendations(
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("10") int pageSize,
      @QueryParam("kind") String kind) {

    SessionUser user = SecurityUtils.getSessionUserOrNull(request);

    Publication.Kind kindEnum = (kind != null && !kind.isBlank())
        ? Publication.Kind.valueOf(kind.toUpperCase())
        : null;

    List<MiniPublication> recs = publicationService.getRecommendations(user, pageSize, page, kindEnum);
    return Response.ok(recs).build();
  }

  /**
   * Registers a like interaction for the current user on the specified publication.
   *
   * @param pubId the id of the publication to like
   * @return 200 OK
   */
  @POST
  @Path("/{id}/like")
  public Response like(@PathParam("id") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    publicationService.likePublication(user, pubId);
    return Response.ok().build();
  }

  /**
   * Removes a previously liked publication from the user's interaction history.
   *
   * @param pubId the id of the publication to unlike
   * @return 200 OK
   */
  @DELETE
  @Path("/{id}/like")
  public Response unlike(@PathParam("id") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    publicationService.unlikePublication(user, pubId);
    return Response.ok().build();
  }

  /**
   * Checks whether the current user has liked the given publication.
   *
   * @param pubId the id of the publication
   * @return {@code {"liked": true}} if liked, otherwise false
   */
  @GET
  @Path("/{id}/like")
  public Response hasLiked(@PathParam("id") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    boolean liked = publicationService.hasLikedPublication(user, pubId);
    return Response.ok(Map.of("liked", liked)).build();
  }

  /**
   * Records a view interaction for the given publication by the current user.
   *
   * @param pubId the id of the publication viewed
   * @return 200 OK
   */
  @POST
  @Path("/{id}/view")
  public Response view(@PathParam("id") int pubId) {
    SessionUser user = SecurityUtils.getSessionUserOrNull(request);
    if (user == null) { return Response.ok().build(); }
    publicationService.viewPublication(user, pubId);
    return Response.ok().build();
  }
}