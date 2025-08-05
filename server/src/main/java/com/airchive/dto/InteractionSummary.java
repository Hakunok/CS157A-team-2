package com.airchive.dto;

import java.time.LocalDateTime;

/**
 * Represents a lightweight summary of an account's interaction with a publication.
 *
 * <p>This record is used for displaying recent user activity such as likes or views.
 *
 * @param pubId
 * @param type
 * @param interacted_at
 */
public record InteractionSummary(
    int pubId,
    String title,
    PublicationInteractionType type,
    LocalDateTime interacted_at) {

  /**
   * Enum representing publication interaction types.
   *
   * <ul>
   *   <li>{@code LIKE} - the user liked the publication</li>
   *   <li>{@code VIEW} - the user viewed the publication</li>
   *   <li>{@code SAVE} - the user saved the publication</li>
   * </ul>
   */
  public enum PublicationInteractionType {
    LIKE, VIEW, SAVE
  }
}