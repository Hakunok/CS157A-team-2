package com.airchive.entity;

import java.time.LocalDateTime;

public record Author(
    Integer id,
    Integer userId,
    String firstName,
    String lastName,
    String bio,
    boolean isUser,
    LocalDateTime createdAt
) {

  public Author(Integer userId, String firstName, String lastName) {
    this(null, userId, firstName, lastName, null, true, null);
  }

  public Author(String firstName, String lastName) {
    this(null, null, firstName, lastName, null, false, null);
  }
}
