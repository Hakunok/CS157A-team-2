package com.airchive.dto;

import com.airchive.entity.Publication;

/**
 * Request body for creating or editing a drafted publication.
 *
 * <p>This record contains the bare minimum metadata needed to begin writing a publication.
 * Authors and topics are not included here and should only be sent once during a publish request.
 *
 * <p>Used by endpoints such as:
 * <ul>
 *   <li>{@code POST /publications}</li>
 *   <li>{@code PUT /publications/{id}}</li>
 * </ul>
 */
public record Draft(
    String title,
    String content,
    String doi,
    String url,
    Publication.Kind kind
) {}