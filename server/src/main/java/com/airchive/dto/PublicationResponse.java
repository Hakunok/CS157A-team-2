package com.airchive.dto;

import com.airchive.entity.Person;
import com.airchive.entity.Publication;
import com.airchive.entity.Publication.Kind;
import com.airchive.entity.Publication.Status;
import com.airchive.entity.Topic;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The full response body representing a complete publication and all associated metadata.
 * <p>
 * This DTO is used by endpoints that return detailed publication information, such as pages that
 * render the publication.
 * <p>
 * Used by endpoints such as {@code GET /publications/{id}}.
 *
 * @param pubId the unique publication id
 * @param title the title of the publication
 * @param content the body content as Markdown or HTML
 * @param doi optional doi for papers
 * @param url optional url for pdf link
 * @param kind the type of publication
 * @param submitterId the id of the submitting user
 * @param submittedAt the time at which the draft was published
 * @param publishedAt the time at which the publication was published
 * @param status the current publication status
 * @param viewCount the number of times viewed
 * @param likeCount the number of likes received
 * @param authors list of authors
 * @param topics list of associated topics
 *
 * @see Publication
 */
public record PublicationResponse(
    int pubId,
    String title,
    String content,
    String doi,
    String url,
    Kind kind,
    Integer submitterId,
    LocalDateTime submittedAt,
    LocalDateTime publishedAt,
    Status status,
    int viewCount,
    int likeCount,
    List<Person>  authors,
    List<Topic> topics
) {

  /**
   * Constructs a full {@code PublicationResponse}, including authors and topics.
   *
   * @param publication the publication entity
   * @param viewCount total view count
   * @param likeCount total like count
   * @param authors ordered list of author entities
   * @param topics list of associated topics
   * @return a fully constructed {@code PublicationResponse}
   */
  public static PublicationResponse from(Publication publication, int viewCount, int likeCount, List<Person> authors, List<Topic> topics) {
    return new PublicationResponse(
        publication.pubId(),
        publication.title(),
        publication.content(),
        publication.doi(),
        publication.url(),
        publication.kind(),
        publication.submitterId(),
        publication.submittedAt(),
        publication.publishedAt(),
        publication.status(),
        viewCount,
        likeCount,
        authors,
        topics);
  }
}