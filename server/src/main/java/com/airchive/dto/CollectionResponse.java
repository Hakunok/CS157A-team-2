package com.airchive.dto;

import com.airchive.entity.Collection;
import java.time.LocalDateTime;

public record CollectionResponse(
  int collectionId,
  int accountId,
  String title,
  String description,
  boolean isDefault,
  boolean isPublic,
  LocalDateTime createdAt
) {
  public static CollectionResponse from(Collection c) {
    return new CollectionResponse(
      c.collectionId(),
      c.accountId(),
      c.title(),
      c.description(),
      c.isDefault(),
      c.isPublic(),
      c.createdAt()
    );
  }
}