package com.airchive.repository;

import com.airchive.entity.Person;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Manages data persistence for {@link Person} entities.
 * This repository handles the creation and retrieval of person records, which
 * represent individuals in the system. It ensures that each
 * person is unique based on their identity email.
 */
public class PersonRepository extends BaseRepository {

  /**
   * Creates a new person record in the database.
   * This method manages its own database connection.
   *
   * @param person The Person object to persist. The ID field is ignored.
   * @return The created Person, now with its database-generated ID.
   * @throws ValidationException if a person with the same identity email already exists.
   */
  public Person create(Person person) {
    return withConnection(conn -> create(person, conn));
  }

  /**
   * Creates a new person record using a provided database connection.
   * This is intended to be used as part of a larger transaction, particularly for
   * account creation.
   *
   * @param person The Person object to persist. The ID field is ignored.
   * @param conn The active database connection.
   * @return The created Person, now with its database-generated ID.
   * @throws ValidationException if a person with the same identity email already exists.
   * @throws EntityNotFoundException if the creation fails and the new person cannot be retrieved.
   */
  public Person create(Person person, Connection conn) {
    if (existsByIdentityEmail(person.identityEmail(), conn)) {
      throw new ValidationException("A person identified by this email already exists.");
    }

    int newId = executeInsertWithGeneratedKey(
        conn,
        "INSERT INTO person (first_name, last_name, identity_email) VALUES (?, ?, ?)",
        person.firstName(),
        person.lastName(),
        person.identityEmail()
    );
    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Person creation failed."));
  }

  /**
   * Finds a person by their unique ID.
   *
   * @param personId The ID of the person to find.
   * @return An {@link Optional} containing the found Person, or empty if not found.
   */
  public Optional<Person> findById(int personId) {
    return withConnection(conn -> findById(personId, conn));
  }

  /**
   * Finds a person by their unique ID using a provided connection.
   *
   * @param personId The ID of the person to find.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Person, or empty if not found.
   */
  public Optional<Person> findById(int personId, Connection conn) {
    return findOne(conn, "SELECT * FROM person WHERE person_id = ?", this::mapRowToPerson, personId);
  }

  /**
   * Finds a person by their unique identity email.
   *
   * @param email The identity email of the person to find.
   * @return An {@link Optional} containing the found Person, or empty if not found.
   */
  public Optional<Person> findByIdentityEmail(String email) {
    return withConnection(conn -> findByIdentityEmail(email, conn));
  }

  /**
   * Finds a person by their unique identity email using a provided connection.
   *
   * @param email The identity email of the person to find.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Person, or empty if not found.
   */
  public Optional<Person> findByIdentityEmail(String email, Connection conn) {
    return findOne(conn, "SELECT * FROM person WHERE identity_email = ?", this::mapRowToPerson, email);
  }

  /**
   * Checks if a person exists with the given identity email.
   *
   * @param email The identity email to check for.
   * @return {@code true} if a person with this email exists, {@code false} otherwise.
   */
  public boolean existsByIdentityEmail(String email) {
    return withConnection(conn -> existsByIdentityEmail(email, conn));
  }

  /**
   * Checks if a person exists with the given identity email using a provided connection.
   *
   * @param email The identity email to check for.
   * @param conn The active database connection.
   * @return {@code true} if a person with this email exists, {@code false} otherwise.
   */
  public boolean existsByIdentityEmail(String email, Connection conn) {
    return exists(conn, "SELECT EXISTS(SELECT 1 FROM person WHERE identity_email = ?)", email);
  }

  /**
   * Maps a row from the 'person' table in a {@link ResultSet} to a {@link Person} object.
   *
   * @param rs The ResultSet to map from.
   * @return The mapped Person object.
   * @throws SQLException if a database access error occurs.
   */
  private Person mapRowToPerson(ResultSet rs) throws SQLException {
    return new Person(
        rs.getInt("person_id"),
        rs.getString("first_name"),
        rs.getString("last_name"),
        rs.getString("identity_email")
    );
  }
}