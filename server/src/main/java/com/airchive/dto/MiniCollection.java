package com.airchive.dto;

import com.airchive.entity.Collection;
import java.time.LocalDateTime;
/**
 * Lightweight representation of a {@link com.airchive.entity.Collection}, that will be used for
 * bare minimum views in the frontend.
 *
 * <p>This record includes only the minimal metadata needed for display like title, default, and
 * publicity.
 *
 * @param collectionId
 * @param title
 * @param isDefault
 * @param isPublic
 * @param createdAt
 */
public record MiniCollection(
    int collectionId,
    String title,
    boolean isDefault,
    boolean isPublic,
    LocalDateTime createdAt
) {

  public static MiniCollection from(Collection collection) {
    return new MiniCollection(
        collection.collectionId(),
        collection.title(),
        collection.isDefault(),
        collection.isPublic(),
        collection.createdAt()
    );
  }
}