package com.airchive.entity;

/**
 * Represents a topic assigned to a publication.
 * <p>
 * This record models a row in the {@code publication_topic} many-to-many relationship table, linking a
 * {@link Publication} to a {@link Topic}.
 * <p>
 * Each publication may be associated with up to three topics, and each topic may be used across many publications.
 *
 * @param pubId the id of the publication
 * @param topicId the id of the linked topic
 */
public record PublicationTopic(
    int pubId,
    int topicId
) {}