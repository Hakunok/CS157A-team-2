package com.airchive.dto;

import java.time.LocalDateTime;

/**
 * Response body representing a summary of a user's interaction with a publication.
 * <p>
 * This DTO is used to display recent activity on a user dashboards or profile pages, inluding interactions
 * such as likes, views, and saves.
 *
 * @param pubId the id of the publication
 * @param title the title of the publication
 * @param type the type of interaction (LIKE, VIEW, SAVE)
 * @param interacted_at the timestamp of when the interaction occurred
 *
 * @see InteractionSummary.PublicationInteractionType
 */
public record InteractionSummary(
    int pubId,
    String title,
    PublicationInteractionType type,
    LocalDateTime interacted_at) {

  /**
   * Enum representing the type of user interaction with a publication
   *
   * <ul>
   *   <li>{@code LIKE} - the user liked the publication</li>
   *   <li>{@code VIEW} - the user viewed the publication</li>
   *   <li>{@code SAVE} - the user saved the publication to a collection</li>
   * </ul>
   */
  public enum PublicationInteractionType {
    LIKE, VIEW, SAVE
  }
}