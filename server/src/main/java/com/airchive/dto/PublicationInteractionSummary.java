package com.airchive.dto;

import java.time.LocalDateTime;

public record PublicationInteractionSummary(
    int pubId,
    PublicationInteractionType type,
    LocalDateTime interacted_at) {

  public enum PublicationInteractionType {
    LIKE, VIEW
  }
}