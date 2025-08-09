package com.airchive.dto;

import com.airchive.entity.Account;

/**
 * Represents an authenticated user stored in the {@link javax.servlet.http.HttpSession}.
 * <p>
 * This record contains only the minimum required information to perform role-based access control
 * and identity checks during an active user session. This should never be exposed to the client.
 * <p>
 * This record should be created after successful login or account registration, stored in
 * {@link javax.servlet.http.HttpSession}, and retrieved using {@link com.airchive.util.SecurityUtils}.
 *
 * @param accountId the id of the authenticated account
 * @param username the username of the user
 * @param isAdmin whether the user is an admin
 * @param role the user's assigned role
 *
 * @see Account
 * @see com.airchive.util.SecurityUtils
 */
public record SessionUser(
    int accountId,
    String username,
    boolean isAdmin,
    Account.Role role
) {

  /**
   * Constructs a {@code SessionUser} from the given {@link Account}.
   *
   * @param account the authenticated user's account
   * @return a {@code SessionUser} for storing in the {@link javax.servlet.http.HttpSession}
   */
  public static SessionUser from(Account account) {
    return new SessionUser(account.accountId(), account.username(), account.isAdmin(), account.role());
  }
}