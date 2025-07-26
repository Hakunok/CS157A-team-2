package com.airchive.dto;

import java.time.LocalDateTime;

public record PublicationInteractionSummary(
    int pubId,
    PublicationInteractionType type,
    LocalDateTime timestamp
) {
  public enum PublicationInteractionType {
    VIEW, LIKE
  }
}