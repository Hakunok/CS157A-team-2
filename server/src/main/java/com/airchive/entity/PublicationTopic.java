package com.airchive.entity;

/**
 * Represents a topic assigned to a publication.
 *
 * <p>This record models a row in the {@code publication_topic} M:N relationship table,
 * linking a {@link Publication} to a {@link Topic}.</p>
 */
public record PublicationTopic(
    int pubId,
    int topicId
) {}