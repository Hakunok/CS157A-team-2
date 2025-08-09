package com.airchive.entity;

/**
 * Represents a person in the system, and used to store identity information linked to a user account
 * or for attributing authorship to publications.
 * <p>
 * This entity is stored in the {@code person} table and is referenced by both {@link Account} and
 * {@link PublicationAuthor}. This record represents the identity for an individual, regardless of whether
 * they are associated with an account.
 * <p>
 * A person may exist independently of an {@link Account}, for example in publication authorship where
 * an author does not exist within the system.
 *
 * @param personId the id of the person
 * @param firstName the person's first name
 * @param lastName the person's last name
 * @param identityEmail the identity email used for unique identification
 * @TODO: It's too late now since we have to submit soon, but ideally we would rethink how we would identity authors not on the system
 */
public record Person(
    Integer personId,
    String firstName,
    String lastName,
    String identityEmail
) {}