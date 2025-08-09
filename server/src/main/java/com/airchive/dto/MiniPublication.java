package com.airchive.dto;

import com.airchive.entity.Publication;
import com.airchive.entity.Topic;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lightweight response body representing a {@link Publication} used for publication list views such as
 * search results, explore pages, and author dashboards.
 * <p>
 * This record includes only the essential metadata for displays such as title, kind, publish date, view/like count,
 * authors, etc.
 * <p>
 * This DTO is used in endpoints like {@code GET /publications/search}, {@code GET /publications/recommendations},
 * {@code GET /publications/my}, etc.
 *
 * @param pubId the publication's unique id
 * @param title the title of the publication
 * @param kind the kind of the publication
 * @param publishedAt the publishing datetime
 * @param viewCount the number of views recorded
 * @param likeCount the number of likes recorded
 * @param authors the minimal first author data
 * @param topics list of topics related to the publication
 *
 * @see Publication
 */
public record MiniPublication(
    int pubId,
    String title,
    Publication.Kind kind,
    LocalDateTime publishedAt,
    int viewCount,
    int likeCount,
    List<MiniPerson> authors,
    List<Topic> topics
) {

  /**
   * Constructs a {@code MiniPublication} from a full {@link Publication} and its metadata.
   *
   * @param pub the publication entity
   * @param viewCount number of views
   * @param likeCount number of likes
   * @param firstAuthor the first author to include (optional)
   * @param topics list of topics associated with the publication (optional)
   * @return a {@code MiniPublication} for lightweight responses
   */
  public static MiniPublication from(
      Publication pub,
      int viewCount,
      int likeCount,
      MiniPerson firstAuthor,
      List<Topic> topics
  ) {
    return new MiniPublication(
        pub.pubId(),
        pub.title(),
        pub.kind(),
        pub.publishedAt(),
        viewCount,
        likeCount,
        (firstAuthor == null) ? List.of() : List.of(firstAuthor),
        topics != null ? topics : List.of()
    );
  }
}