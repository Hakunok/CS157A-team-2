package com.airchive.entity;

import java.time.LocalDateTime;

public record AuthorRequest(
    Integer id,
    Integer userId,
    Status status,
    LocalDateTime createdAt,
    LocalDateTime decidedAt
) {
  public enum Status { PENDING, APPROVED, REJECTED }

  public AuthorRequest(Integer userId) {
    this(null, userId, null, null, null);
  }
}