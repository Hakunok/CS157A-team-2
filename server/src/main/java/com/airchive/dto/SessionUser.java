package com.airchive.dto;

import com.airchive.entity.Account;

public record SessionUser(
    Integer userId,
    String username,
    String permission
) {
  public static SessionUser from(Account user) {
    return new SessionUser(
        user.id(),
        user.username(),
        user.permission().name()
    );
  }
}