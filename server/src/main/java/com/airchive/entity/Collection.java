package com.airchive.entity;

import java.time.LocalDateTime;

/**
 * Represents a user-created collection of saved publications.
 *
 * <p>A collection is created and owned by an {@code Account}, and is intended to be used to
 * organize publications into meaningful groups (e.g., "Favorites", "To Read", "Information
 * Security", etc).
 *
 * <p>Collections support privacy and default flags:
 * <ul>
 *   <li>{@code isDefault} - marks the system-managed default collection (one per account)</li>
 *   <li>{@code isPublic} - determines if the collection can be viewed by others</li>
 * </ul>
 *
 * <p>Each collection may contain multiple publications via the {@code collection_item} table.
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