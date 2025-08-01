package com.airchive.entity;

/**
 * Represents a person in the system, typically used as identity information for a user account
 * or to attribute authorship.
 *
 * <p>This entity is stored in the {@code person} table and is referenced by {@link Account} and
 * {@link PublicationAuthor}.
 *
 * @param personId
 * @param firstName
 * @param lastName
 * @param identityEmail
 */
public record Person(
    Integer personId,
    String firstName,
    String lastName,
    String identityEmail
) {}