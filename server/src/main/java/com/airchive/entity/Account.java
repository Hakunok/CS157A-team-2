package com.airchive.entity;

import java.time.LocalDateTime;

/**
 * Represents a persistent user account in the system, backed by the {@code account} table.
 * <p>
 * Each {@code Account} is associated with a {@link Person} entity and stores login credentials,
 * assigned role, admin status, and metadata such as creation time.
 * <p>
 * This record is used internally by the repository and service layers and managing authentication, authorization,
 * and user-level access control. This shouldn't be exposed to clients.
 *
 * @param accountId the account's unique id
 * @param personId the id of the linked {@link Person}
 * @param email the user's email address
 * @param username the user's username
 * @param passwordHash the securely hashed password
 * @param role the user's role
 * @param isAdmin whether the account has admin privileges
 * @param createdAt the time the account was created
 *
 * @see Person
 * @see com.airchive.dto.UserResponse
 * @see com.airchive.dto.AccountRegisterRequest
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
   * Defines the role assigned to an {@link Account}.
   * <ul>
   *   <li>{@code READER} – can view content, interact with publications, and submit upgrade requests</li>
   *   <li>{@code AUTHOR} – can create and publish publications</li>
   * </ul>
   */
  public enum Role {
    READER, AUTHOR
  }
}