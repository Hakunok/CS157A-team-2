package com.airchive.dto;

import com.airchive.entity.Collection;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response body returned by endpoints that expose detailed views of a collection, including its metadata and
 * a list of contained publications.
 * <p>
 * This DTO is used by endpoints such as {@code GET /collections/{id}} and {@code POST /collections}.
 * <p>
 * Each {@code CollectionResponse} includes the collection's metadata, visibility information, and a
 * list of lightweight publications.
 *
 * @param collectionId the unique ID of the collection
 * @param title the title of the collection
 * @param description the collection description (can be empty or rich text)
 * @param isDefault whether this is the default collection for the user
 * @param isPublic whether the collection is publicly visible
 * @param createdAt the creation timestamp
 * @param publications the list of publications in the collection
 *
 * @see MiniPublication
 */
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