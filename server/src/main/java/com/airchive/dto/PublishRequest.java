package com.airchive.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request body used to publish draft publication.
 *
 * <p>This record includes the list of author and topic ids that should be attached to
 * the publication as well as an optional {@code publishedAt} timestamp.
 *
 * <p>Used by the {@code POST /publications/{id}/publish} endpoint.
 *
 * @param authorIds
 * @param topicIds
 * @param publishedAt
 */
public record PublishRequest(
    List<Integer> authorIds,
    List<Integer> topicIds,
    LocalDateTime publishedAt
) {}