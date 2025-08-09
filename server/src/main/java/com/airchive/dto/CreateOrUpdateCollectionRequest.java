package com.airchive.dto;

/**
 * Request body for creating or updating a user collection.
 * <p>
 * This DTO is used by: {@code POST /collections} and {@code PUT /collections/{id}}.
 * <p>
 * The requesting client must provide a title, optional description, and visibility.
 *
 * @param title the collection's title
 * @param description the collection's description
 * @param isPublic whether the collection is public
 */
public record CreateOrUpdateCollectionRequest(
  String title,
  String description,
  boolean isPublic
) {}