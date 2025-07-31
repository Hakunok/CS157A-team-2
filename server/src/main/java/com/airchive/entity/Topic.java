package com.airchive.entity;

/**
 * Represents a curated topic used to classify publications.
 *
 * <p>Topics are associated with publications through the {@code publication_topic} M:N
 * relationship table, enabling users to filter, search, and explore content based on
 * subject areas.
 */
public record Topic(
    int topicId,
    String code,
    String fullName
) {}