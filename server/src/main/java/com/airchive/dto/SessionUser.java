package com.airchive.dto;

import com.airchive.entity.Account;

/**
 * Represents a simplified authenticated user stored in the HTTP session.
 *
 * <p>This record is used server-side only and contains the bare minimum information to perform
 * access control and role validation during a session.
 *
 * <p>This record is typically set in the session after login or account creation and retrieved
 * via {@link com.airchive.util.SecurityUtils}.
 */
public record SessionUser(
    int accountId,
    String username,
    boolean isAdmin,
    Account.Role role
) {

  /**
   * Constrcuts a {@code SessionUser} from the given {@link Account}.
   *
   * @param account the authenticated user's account
   * @return a {@code SessionUser} containing only bare minimum session-safe fields
   */
  public static SessionUser from(Account account) {
    return new SessionUser(account.accountId(), account.username(), account.isAdmin(), account.role());
  }
}