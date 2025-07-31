package com.airchive.dto;

import com.airchive.entity.Person;
import com.airchive.entity.Publication;
import com.airchive.entity.Publication.Kind;
import com.airchive.entity.Publication.Status;
import com.airchive.entity.Topic;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The full response body representing a single publication with all associated metadata needed
 * for the frontend.
 *
 * <p>This record is used when returning the complete details of a publication, such as on a
 * detail page or preview modal. It includes the publication's title, content, submission
 * metadata, publication status, and all associated authors and topics.
 *
 * <p>Used by endpoints such as {@code GET /publications/{id}}.
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
    List<Person>  authors,
    List<Topic> topics
) {

  /**
   * Factory method to build a {@code PublicationResponse} from a {@link Publication} entity and
   * its associated authors and topics.
   *
   * @param publication the publication entity
   * @param authors the ordered list of authors
   * @param topics the list of topics associated with the publication
   * @return a fully populated {@code PublicationResponse}
   */
  public static PublicationResponse from(Publication publication, List<Person> authors, List<Topic> topics) {
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
        authors,
        topics);
  }
}