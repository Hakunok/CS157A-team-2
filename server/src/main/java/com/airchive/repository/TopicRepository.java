package com.airchive.repository;

import com.airchive.entity.Topic;
import com.airchive.exception.EntityNotFoundException;

import com.airchive.exception.ValidationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Manages data persistence for {@link Topic} entities.
 * This repository handles all CRUD operations for topics, which are used to categorize
 * publications. It ensures topic codes are unique and provides search functionality.
 */
public class TopicRepository extends BaseRepository {

  /**
   * Creates a new topic in the database.
   * This method manages its own database connection.
   *
   * @param topic The Topic object to persist. The ID field is ignored.
   * @return The created Topic, now with its database-generated ID.
   * @throws ValidationException if a topic with the same code already exists.
   */
  public Topic create(Topic topic) {
    return withConnection(conn -> create(topic, conn));
  }

  /**
   * Creates a new topic using a provided database connection.
   *
   * @param topic The Topic object to persist. The ID field is ignored.
   * @param conn The active database connection.
   * @return The created Topic, now with its database-generated ID.
   * @throws ValidationException if a topic with the same code already exists.
   * @throws EntityNotFoundException if the creation fails and the new topic cannot be retrieved.
   */
  public Topic create(Topic topic, Connection conn) {
    if (existsByCode(topic.code(), conn)) {
      throw new ValidationException("A topic with this code already exists.");
    }
    int newId = executeInsertWithGeneratedKey(
        conn,
        "INSERT INTO topic (code, full_name) VALUES (?, ?)",
        topic.code(),
        topic.fullName()
    );
    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Topic creation failed."));
  }

  /**
   * Updates an existing topic's code and full name.
   *
   * @param topicId The ID of the topic to update.
   * @param code The new code for the topic.
   * @param fullName The new full name for the topic.
   * @throws ValidationException if the new code is already in use by another topic.
   * @throws EntityNotFoundException if the topic to update is not found.
   */
  public void update(int topicId, String code, String fullName) {
    withConnection(conn -> {
      update(topicId, code, fullName, conn);
      return null;
    });
  }

  /**
   * Updates an existing topic's code and full name using a provided connection.
   *
   * @param topicId The ID of the topic to update.
   * @param code The new code for the topic.
   * @param fullName The new full name for the topic.
   * @param conn The active database connection.
   * @throws ValidationException if the new code is already in use by another topic.
   * @throws EntityNotFoundException if the topic to update is not found.
   */
  public void update(int topicId, String code, String fullName, Connection conn) {
    Optional<Topic> existing = findByCode(code, conn);
    if (existing.isPresent() && existing.get().topicId() != topicId) {
      throw new ValidationException("A different topic with this code already exists.");
    }

    int rows = executeUpdate(
        conn,
        "UPDATE topic SET code = ?, full_name = ? WHERE topic_id = ?",
        code, fullName, topicId
    );
    if (rows == 0) {
      throw new EntityNotFoundException("Topic not found for update.");
    }
  }

  /**
   * Deletes a topic from the database.
   *
   * @param topicId The ID of the topic to delete.
   * @throws EntityNotFoundException if the topic to delete is not found.
   */
  public void delete(int topicId) {
    withConnection(conn -> {
      delete(topicId, conn);
      return null;
    });
  }

  /**
   * Deletes a topic using a provided database connection.
   *
   * @param topicId The ID of the topic to delete.
   * @param conn The active database connection.
   * @throws EntityNotFoundException if the topic to delete is not found.
   */
  public void delete(int topicId, Connection conn) {
    int rows = executeUpdate(
        conn,
        "DELETE FROM topic WHERE topic_id = ?",
        topicId
    );
    if (rows == 0) {
      throw new EntityNotFoundException("Topic not found for deletion.");
    }
  }

  /**
   * Finds a topic by its unique ID.
   *
   * @param topicId The ID of the topic to find.
   * @return An {@link Optional} containing the found Topic, or empty if not found.
   */
  public Optional<Topic> findById(int topicId) {
    return withConnection(conn -> findById(topicId, conn));
  }

  /**
   * Finds a topic by its unique ID using a provided connection.
   *
   * @param topicId The ID of the topic to find.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Topic, or empty if not found.
   */
  public Optional<Topic> findById(int topicId, Connection conn) {
    return findOne(conn, "SELECT * FROM topic WHERE topic_id = ?", this::mapRowToTopic, topicId);
  }

  /**
   * Finds a topic by its unique code.
   *
   * @param code The code of the topic to find.
   * @return An {@link Optional} containing the found Topic, or empty if not found.
   */
  public Optional<Topic> findByCode(String code) {
    return withConnection(conn -> findByCode(code, conn));
  }

  /**
   * Finds a topic by its unique code using a provided connection.
   *
   * @param code The code of the topic to find.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Topic, or empty if not found.
   */
  public Optional<Topic> findByCode(String code, Connection conn) {
    return findOne(conn, "SELECT * FROM topic WHERE code = ?", this::mapRowToTopic, code);
  }

  /**
   * Checks if a topic exists with the given code.
   *
   * @param code The code to check for.
   * @return {@code true} if a topic with this code exists, {@code false} otherwise.
   */
  public boolean existsByCode(String code) {
    return withConnection(conn -> existsByCode(code, conn));
  }

  /**
   * Checks if a topic exists with the given code using a provided connection.
   *
   * @param code The code to check for.
   * @param conn The active database connection.
   * @return {@code true} if a topic with this code exists, {@code false} otherwise.
   */
  public boolean existsByCode(String code, Connection conn) {
    return exists(conn, "SELECT EXISTS(SELECT 1 FROM topic WHERE code = ?)", code);
  }

  /**
   * Retrieves all topics from the database.
   *
   * @return A {@link List} of all topics, ordered alphabetically by full name.
   */
  public List<Topic> findAll() {
    return withConnection(this::findAll);
  }

  /**
   * Retrieves all topics using a provided connection.
   *
   * @param conn The active database connection.
   * @return A {@link List} of all topics, ordered alphabetically by full name.
   */
  public List<Topic> findAll(Connection conn) {
    return findMany(conn, "SELECT * FROM topic ORDER BY full_name ASC", this::mapRowToTopic);
  }

  /**
   * Searches for topics where the code or full name matches a given query.
   * The search is case-insensitive.
   *
   * @param query The search term.
   * @return A {@link List} of matching topics, ordered alphabetically by full name.
   */
  public List<Topic> search(String query) {
    return withConnection(conn -> search(query, conn));
  }

  /**
   * Searches for topics using a provided connection.
   *
   * @param query The search term.
   * @param conn The active database connection.
   * @return A {@link List} of matching topics, ordered alphabetically by full name.
   */
  public List<Topic> search(String query, Connection conn) {
    String like = "%" + query.toLowerCase() + "%";
    return findMany(
        conn,
        "SELECT * FROM topic WHERE LOWER(code) LIKE ? OR LOWER(full_name) LIKE ? ORDER BY full_name ASC",
        this::mapRowToTopic,
        like,
        like
    );
  }

  /**
   * Maps a row from the 'topic' table in a {@link ResultSet} to a {@link Topic} object.
   *
   * @param rs The ResultSet to map from.
   * @return The mapped Topic object.
   * @throws SQLException if a database access error occurs.
   */
  private Topic mapRowToTopic(ResultSet rs) throws SQLException {
    return new Topic(
        rs.getInt("topic_id"),
        rs.getString("code"),
        rs.getString("full_name")
    );
  }
}