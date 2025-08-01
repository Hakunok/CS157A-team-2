package com.airchive.dto;

import com.airchive.entity.Person;

/**
 * Lightweight representation of a person, that will be used in publication summaries or list views.
 *
 * <p>This record includes the bare-minimum information for identifying a {@code person},
 * primarily for frontend UI.
 *
 * <p>Used within {@link MiniPublication} to avoid sending full {@link Person} records.
 *
 * @param personId
 * @param fullName
 */
public record MiniPerson(
    int personId,
    String fullName
) {

  /**
   * Converts a {@link Person} entity into a {@code MiniPerson} for lightweight display.
   *
   * @param person the {@link Person} to convert
   * @return a {@code MiniPerson} containing the id and full name
   */
  public static MiniPerson from(Person person) {
    return new MiniPerson(person.personId(), person.firstName() + " " + person.lastName());
  }
}