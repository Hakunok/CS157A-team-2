package com.airchive.dto;

import com.airchive.entity.Collection;
import java.time.LocalDateTime;
/**
 * Lightweight response body representing a {@link Collection} for minimal client display.
 * <p>
 * This DTO is used in explore pages and dashboards where only basic collection metadata is needed.
 * <p>
 * Used by endpoints such as {@code GET /collections/my} and {@code GET /collections/recommendations}.
 *
 * @param collectionId the unique ID of the collection
 * @param title the title of the collection
 * @param isDefault whether this is the default collection for the user
 * @param isPublic whether the collection is publicly visible
 * @param createdAt the creation timestamp
 *
 * @see Collection
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