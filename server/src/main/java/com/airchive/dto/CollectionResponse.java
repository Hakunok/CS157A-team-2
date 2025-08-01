package com.airchive.dto;

import com.airchive.entity.Collection;
import java.time.LocalDateTime;
import java.util.List;

public record CollectionResponse(
    int collectionId,
    String title,
    String description,
    boolean isDefault,
    boolean isPublic,
    LocalDateTime createdAt,
    List<MiniPublication> publications
) {
  public static CollectionResponse from(Collection collection, List<MiniPublication> publications) {
    return new CollectionResponse(
        collection.collectionId(),
        collection.title(),
        collection.description(),
        collection.isDefault(),
        collection.isPublic(),
        collection.createdAt(),
        publications
    );
  }
}