package com.airchive.dto;

import com.airchive.entity.Publication;
import com.airchive.entity.Topic;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record representing a publication for lightweight responses needed for search results,
 * recommendations, or an author's dashboard.
 *
 * <p>This record includes only minimal metadata required for display: title, kind, publish
 * timestamp, author names, and topics.
 * It does not include content, submitter id, or draft-related fields.
 *
 * <p>Returned by endpoints such as:
 * <ul>
 *   <li>{@code GET /publications/search}</li>
 *   <li>{@code GET /publications/recommendations}</li>
 *   <li>{@code GET /publications/my}</li>
 * </ul>
 *
 * @param pubId
 * @param title
 * @param kind
 * @param publishedAt
 * @param viewCount
 * @param likeCount
 * @param authors
 * @param topics
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