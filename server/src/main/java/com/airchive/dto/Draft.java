package com.airchive.dto;

import com.airchive.entity.Publication;

/**
 * Request and response body for creating or updating a drafted publication.
 * <p>
 * This record contains the metadata required to create or edit a draft. It does not include authors
 * or topics, these should be submitted separately within the {@link PublishRequest} when publishing.
 * <p>
 * This DTO is used by the endpoints {@code POST /publications} and {@code PUT /publications/{id}}.
 *
 * @param title the title of the draft
 * @param content the draft content in Markdown/HTML
 * @param doi optional DOI for papers
 * @param url optional external link for a paper's PDF
 * @param kind the type of publication being drafted
 *
 * @see Publication.Kind
 */
public record Draft(
    String title,
    String content,
    String doi,
    String url,
    Publication.Kind kind
) {}