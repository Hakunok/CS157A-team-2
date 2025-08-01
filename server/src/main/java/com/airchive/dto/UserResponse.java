package com.airchive.dto;

import com.airchive.entity.Account;
import com.airchive.entity.Person;

/**
 * Response DTO reprsenting a full user profile for frontend.
 *
 * <p>This record combines information from the {@link Account} and {@link Person} and provides a
 * complete user view, including login credentials, role, and identity fields.
 *
 * <p>This record should be sent to the frontend for authentication and user profiles.
 *
 * @param accountId
 * @param username
 * @param email
 * @param isAdmin
 * @param role
 * @param firstName
 * @param lastName
 */
public record UserResponse(
    int accountId,
    String username,
    String email,
    boolean isAdmin,
    Account.Role role,
    String firstName,
    String lastName
) {

  /**
   * Constructs a {@code UserResponse} by combining an {@link Account} and its associated
   * {@link Person} record.
   *
   * @param account the user's account data (credentials, role, etc.)
   * @param person the user's personal data (name fields)
   * @return a populated {@code UserResponse} record
   */
  public static UserResponse from(Account account, Person person) {
    return new UserResponse(
        account.accountId(),
        account.username(),
        account.email(),
        account.isAdmin(),
        account.role(),
        person.firstName(),
        person.lastName()
    );
  }
}