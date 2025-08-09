package com.airchive.entity;

import java.time.LocalDateTime;

/**
 * Represents a request made by a user account to be promoted to author status.
 * <p>
 * This record maps to the {@code author_request} table and tracks the account's request
 * to become an author, including its current {@link Status} and the timestamp of submission.
 * <p>
 * This record is used by the service and repository layers to manage author upgrade flows, including approval
 * and status checks.
 *
 * @param accountId the id of the requesting account
 * @param status the current status of the request
 * @param requestedAt the timestamp when the request was made
 *
 * @see com.airchive.dto.PendingAuthorRequest
 */
public record AuthorRequest(
    int accountId,
    Status status,
    LocalDateTime requestedAt
) {

  /**
   * Enum representing the state of an {@link AuthorRequest}.
   * <ul>
   *   <li>{@code PENDING} – the request is awaiting approval</li>
   *   <li>{@code APPROVED} – the request has been approved and the account is now an author</li>
   * </ul>
   */
  public enum Status {
    PENDING, APPROVED
  }
}