package com.airchive.entity;

import java.time.LocalDateTime;

/**
 * Represents a user-created collection of saved publications.
 * <p>
 * A {@code Collection} is created and owned by an {@link Account}, and is used to organize publications
 * into groups.
 * <p>
 * Each account may have multiple collections, including one system-managed default collection. Collections can
 * be private or public, and they may contain many publications via the {@code collection_item} table.
 *
 * @param collectionId the collection's unique id
 * @param accountId the id of the account that owns this collection
 * @param title the title of the collection
 * @param description optional description of the collection
 * @param isDefault whether this is the default collection for the account
 * @param isPublic whether the collection is publicly visible
 * @param createdAt the timestamp of when the collection was created
 *
 * @see com.airchive.dto.CollectionResponse
 * @see com.airchive.dto.MiniCollection
 */
public record Collection(
    int collectionId,
    int accountId,
    String title,
    String description,
    boolean isDefault,
    boolean isPublic,
    LocalDateTime createdAt
) {}