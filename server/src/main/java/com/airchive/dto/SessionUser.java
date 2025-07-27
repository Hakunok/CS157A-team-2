package com.airchive.dto;

import com.airchive.entity.Account;

public record SessionUser(
    int accountId,
    String username,
    boolean isAdmin,
    Account.Role role
) {
  public static SessionUser from(Account account) {
    return new SessionUser(account.accountId(), account.username(), account.isAdmin(), account.role());
  }
}