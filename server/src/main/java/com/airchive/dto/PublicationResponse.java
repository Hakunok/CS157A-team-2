package com.airchive.dto;

import com.airchive.entity.Publication;

import java.time.LocalDateTime;
import java.util.List;

public record PublicationResponse(
    int pubId,
    String title,
    String abstractText,
    String content,
    String doi,
    String url,
    Publication.Kind kind,
    int viewCount,
    int likeCount,
    Publication.Status status,
    LocalDateTime publishedAt,
    LocalDateTime updatedAt,
    Integer submitterId,
    List<Integer> authorIds,
    List<Integer> topicIds
) {
  public static PublicationResponse fromPublication(
      Publication pub,
      List<Integer> authorIds,
      List<Integer> topicIds
  ) {
    return new PublicationResponse(
        pub.pubId(),
        pub.title(),
        pub.abstractText(),
        pub.content(),
        pub.doi(),
        pub.url(),
        pub.kind(),
        pub.viewCount(),
        pub.likeCount(),
        pub.status(),
        pub.publishedAt(),
        pub.updatedAt(),
        pub.submitterId(),
        authorIds,
        topicIds
    );
  }
}
