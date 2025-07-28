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

  public enum Interaction {
    LIKE(3.0),
    VIEW(0.5),
    SAVE(2.5);

    private final double affinityWeight;

    Interaction(double affinityWeight) {
      this.affinityWeight = affinityWeight;
    }

    public double getAffinityWeight() {
      return affinityWeight;
    }
  }
}