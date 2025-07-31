package com.airchive.entity;

import java.time.LocalDateTime;

/**
 * Represents a request made by a user account to become an author.
 *
 * <p>This record models the {@code author_request} table and tracks the status and request date
 * of a user's role upgrade request.
 */
public record AuthorRequest(
    int accountId,
    Status status,
    LocalDateTime requestedAt
) {

  /**
   * Indicates the status of the {@link AuthorRequest}.
   */
  public enum Status {
    PENDING, APPROVED
  }
}