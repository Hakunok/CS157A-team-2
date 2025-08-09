package com.airchive.entity;

/**
 * Represents a curated topic used to classify and organize publications.
 * <p>
 * Topics allow publications to be grouped by subject are and are used for filtering, searching, and powering
 * content-based recommendations.
 * <p>
 * Topics are associated with publications via the {@code publication_topic} many-to-many relationship table.
 *
 * @param topicId the topic's unique id
 * @param code the short code for a topic
 * @param fullName the full name of a topic
 */
public record Topic(
    int topicId,
    String code,
    String fullName
) {}