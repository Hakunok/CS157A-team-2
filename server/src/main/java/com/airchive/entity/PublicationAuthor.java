package com.airchive.entity;

/**
 * Represents an author assigned to a publication, including their order of authorship.
 * <p>
 * This record models a row in the {@code publication_author} many-to-many relationship table, linking
 * a {@link Publication} to a {@link Person} with an associated author order.
 * <p>
 * Author order is used to preserve first author, corresponding author, and other author/credit based
 * information in a multi-authored publication.
 *
 * @param pubId the id of the linked publication
 * @param personId the id of the linked person
 * @param authorOrder the position of the author in the author list
 *
 * @see Publication
 * @see Person
 */
public record PublicationAuthor(
    int pubId,
    int personId,
    int authorOrder
) {}