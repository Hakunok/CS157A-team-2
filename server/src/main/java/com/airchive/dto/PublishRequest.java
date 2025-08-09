package com.airchive.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request body for publishing a drafted publication.
 * <p>
 * This record is submitted when finalizing a draft, attaching authors and topics, and optionally
 * a publish date.
 * <p>
 * Used by the {@code POST /publications/{id}/publish} endpoint.
 *
 * @param authorIds list of author ids to associate with the publication
 * @param topicIds list of topics ids to tag the publication
 * @param publishedAt optional publish timestamp, which defaults to the current timestamp
 */
public record PublishRequest(
    List<Integer> authorIds,
    List<Integer> topicIds,
    LocalDateTime publishedAt
) {}