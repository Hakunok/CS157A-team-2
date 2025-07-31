package com.airchive.entity;

/**
 * Represents an author assigned to a publication, including their order of authorship.
 *
 * <p>This record models a row in the {@code publication_author} M:N relationship table,
 * linking a
 * {@link Publication} to a {@link Person} as an author.</p>
 */
public record PublicationAuthor(
    int pubId,
    int personId,
    int authorOrder
) {}
