package com.airchive.entity;

import java.time.LocalDateTime;

public record Collection(
    Integer id,
    Integer accountId,
    String title,
    String description,
    boolean isDefault,
    boolean isPublic,
    LocalDateTime createdAt
) {
  public Collection(Integer accountId, String title, String description, boolean isDefault, boolean isPublic) {
    this(null, accountId, title, description, isDefault, isPublic, null);
  }
}