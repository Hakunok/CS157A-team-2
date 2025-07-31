package com.airchive.dto;

import com.airchive.entity.Publication;
import com.airchive.entity.Topic;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record representing a publication for lightweight responses needed for search results,
 * recommendations, or an author's dashboard.
 *
 * <p>This record includes only minimal metadata required for display: title, kind, publish
 * timestamp, author names, and topics.
 * It does not include content, submitter id, or draft-related fields.
 *
 * <p>Returned by endpoints such as:
 * <ul>
 *   <li>{@code GET /publications/search}</li>
 *   <li>{@code GET /publications/recommendations}</li>
 *   <li>{@code GET /publications/my}</li>
 * </ul>
 */
public record MiniPublication(
    int pubId,
    String title,
    Publication.Kind kind,
    LocalDateTime publishedAt,
    List<MiniPerson> authors,
    List<Topic> topics
) {}