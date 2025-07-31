package com.airchive.entity;

import java.time.LocalDateTime;

/**
 * Represents a persistent user account in the system, backed by the {@code account} table.
 *
 * <p>Each {@code Account} is linked to a {@link Person} entity and stores credentials, role
 * information, and timestamps for the account.
 *
 * <p>This record should be used primarily between the repository and service layer.
 */
public record Account(
    int accountId,
    int personId,
    String email,
    String username,
    String passwordHash,
    Role role,
    boolean isAdmin,
    LocalDateTime createdAt
) {

  /**
   * Indicates the role of an {@link Account}.
   * <ul>
   *   <li>{@code READER} - can view and interact with content</li>
   *   <li>{@code AUTHOR} - can submit and manage publications</li>
   * </ul>
   */
  public enum Role {
    READER, AUTHOR
  }
}