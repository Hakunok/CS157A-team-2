package com.airchive.entity;

import java.time.LocalDateTime;

public record Publication(
    int pubId,
    String title,
    String abstractText,
    String content,
    String doi,
    String url,
    Kind kind,
    Integer submitterId,
    LocalDateTime publishedAt,
    LocalDateTime updatedAt,
    int viewCount,
    int likeCount,
    Status status
) {
  public enum Kind {
    PAPER, BLOG, ARTICLE
  }

  public enum Status {
    PUBLISHED, UNPUBLISHED, DRAFT, REMOVED
  }
}
