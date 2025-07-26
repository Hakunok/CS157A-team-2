package com.airchive.entity;

import java.time.LocalDateTime;

public record Publication(
    int pubId,
    String title,
    String content,
    String doi,
    String url,
    Kind kind,
    Integer submitterId,
    Integer correspondingAuthorId,
    LocalDateTime submittedAt,
    LocalDateTime publishedAt,
    Status status
) {
  public enum Kind {
    PAPER, BLOG, ARTICLE
  }

  public enum Status {
    PUBLISHED, DRAFT
  }
}