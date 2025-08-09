package com.airchive.dto;

import com.airchive.entity.Person;

/**
 * Lightweight response body representing a {@link Person}, used for publication metadata.
 * <p>
 * This DTO includes only the minimal identity information required for publication display, such as the person's
 * full name.
 * <p>
 * Used primarily inside {@link MiniPublication} to represent authors.
 *
 * @param personId the unique id of the person
 * @param fullName the full display name of the person
 *
 * @see Person
 * @see MiniPublication
 */
public record MiniPerson(
    int personId,
    String fullName
) {

  /**
   * Creates a {@code MiniPerson} from a full {@link Person} entity.
   *
   * @param person the {@link Person} to convert
   * @return a minimal DTO with only the ID and full name
   */
  public static MiniPerson from(Person person) {
    return new MiniPerson(person.personId(), person.firstName() + " " + person.lastName());
  }
}