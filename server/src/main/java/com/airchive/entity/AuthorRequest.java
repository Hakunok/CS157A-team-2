package com.airchive.entity;

import java.time.LocalDateTime;

public record AuthorRequest(
    int accountId,
    Status status,
    LocalDateTime requestedAt
) {
  public enum Status {
    PENDING, APPROVED
  }
}