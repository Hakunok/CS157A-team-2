package com.airchive.entity;

import java.time.LocalDateTime;

public record User(
    Integer id,
    String username,
    String firstName,
    String lastName,
    String email,
    String passwordHash,
    Permission permission,
    Status status,
    LocalDateTime createdAt
) {
  public enum Permission { READER, AUTHOR, ADMIN }
  public enum Status { ACTIVE, SUSPENDED, DELETED }

  public User(String username, String firstName, String lastName, String email, String passwordHash) {
    this(
        null,
        username,
        firstName,
        lastName,
        email,
        passwordHash,
        null,
        null,
        null
    );
  }
}