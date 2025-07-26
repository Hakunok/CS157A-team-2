package com.airchive.entity;

import java.time.LocalDateTime;

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
  public enum Role {
    READER, AUTHOR
  }
}