package com.airchive.repository;

import com.airchive.entity.PublicationTopic;
import com.airchive.entity.Topic;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages the relationship between publications and topics by interacting
 * with the `publication_topic` relationship table. This repository provides methods
 * to associate publications with topics, remove associations, and query these relationships.
 */
public class PublicationTopicRepository extends BaseRepository {

  /**
   * Associates a topic with a publication.
   * This method manages its own database connection.
   *
   * @param pubId The ID of the publication.
   * @param topicId The ID of the topic to add.
   * @throws ValidationException if the topic is already associated with the publication.
   */
  public void addTopic(int pubId, int topicId) {
    withConnection(conn -> {
      addTopic(pubId, topicId, conn);
      return null;
    });
  }

  /**
   * Associates a topic with a publication using a provided database connection.
   *
   * @param pubId The ID of the publication.
   * @param topicId The ID of the topic to add.
   * @param conn The active database connection.
   * @throws ValidationException if the topic is already associated with the publication.
   */
  public void addTopic(int pubId, int topicId, Connection conn) {
    if (exists(pubId, topicId, conn)) {
      throw new ValidationException("This topic is already added to the publication.");
    }

    executeUpdate(conn, "INSERT INTO publication_topic (pub_id, topic_id) VALUES (?, ?)",
        pubId, topicId);
  }

  /**
   * Removes the association between a publication and a topic.
   *
   * @param pubId The ID of the publication.
   * @param topicId The ID of the topic to remove.
   * @throws EntityNotFoundException if the topic is not associated with the publication.
   */
  public void removeTopic(int pubId, int topicId) {
    withConnection(conn -> {
      removeTopic(pubId, topicId, conn);
      return null;
    });
  }

  /**
   * Removes the association between a publication and a topic using a provided connection.
   *
   * @param pubId The ID of the publication.
   * @param topicId The ID of the topic to remove.
   * @param conn The active database connection.
   * @throws EntityNotFoundException if the topic is not associated with the publication.
   */
  public void removeTopic(int pubId, int topicId, Connection conn) {
    int rows = executeUpdate(conn,
        "DELETE FROM publication_topic WHERE pub_id = ? AND topic_id = ?",
        pubId, topicId);
    if (rows == 0) {
      throw new EntityNotFoundException("Topic not found for this publication.");
    }
  }

  /**
   * Checks if a publication is associated with a specific topic.
   *
   * @param pubId The ID of the publication.
   * @param topicId The ID of the topic.
   * @return {@code true} if the association exists, {@code false} otherwise.
   */
  public boolean exists(int pubId, int topicId) {
    return withConnection(conn -> exists(pubId, topicId, conn));
  }

  /**
   * Checks if a publication is associated with a topic using a provided connection.
   *
   * @param pubId The ID of the publication.
   * @param topicId The ID of the topic.
   * @param conn The active database connection.
   * @return {@code true} if the association exists, {@code false} otherwise.
   */
  public boolean exists(int pubId, int topicId, Connection conn) {
    return exists(conn,
        "SELECT EXISTS(SELECT 1 FROM publication_topic WHERE pub_id = ? AND topic_id = ?)",
        pubId, topicId);
  }

  /**
   * Retrieves all topic associations for a given publication.
   *
   * @param pubId The ID of the publication.
   * @return A {@link List} of {@link PublicationTopic} association objects.
   */
  public List<PublicationTopic> findTopicsByPublication(int pubId) {
    return withConnection(conn -> findTopicsByPublication(pubId, conn));
  }

  /**
   * Retrieves all topic associations for a publication using a provided connection.
   *
   * @param pubId The ID of the publication.
   * @param conn The active database connection.
   * @return A {@link List} of {@link PublicationTopic} association objects.
   */
  public List<PublicationTopic> findTopicsByPublication(int pubId, Connection conn) {
    return findMany(
        conn,
        "SELECT * FROM publication_topic WHERE pub_id = ?",
        this::mapRowToPublicationTopic,
        pubId
    );
  }

  /**
   * Retrieves a list of all topic IDs associated with a specific publication.
   *
   * @param pubId The ID of the publication.
   * @return A {@link List} of topic IDs.
   */
  public List<Integer> findTopicIdsByPublication(int pubId) {
    return withConnection(conn -> findTopicIdsByPublication(pubId, conn));
  }

  /**
   * Retrieves all topic IDs for a publication using a provided connection.
   *
   * @param pubId The ID of the publication.
   * @param conn The active database connection.
   * @return A {@link List} of topic IDs.
   */
  public List<Integer> findTopicIdsByPublication(int pubId, Connection conn) {
    return findColumnMany(
        conn,
        "SELECT topic_id FROM publication_topic WHERE pub_id = ?",
        Integer.class,
        pubId
    );
  }

  /**
   * Retrieves all publication IDs associated with a specific topic.
   *
   * @param topicId The ID of the topic.
   * @return A {@link List} of publication IDs.
   */
  public List<Integer> findPublicationIdsByTopic(int topicId) {
    return withConnection(conn -> findPublicationIdsByTopic(topicId, conn));
  }

  /**
   * Retrieves all publication IDs for a topic using a provided connection.
   *
   * @param topicId The ID of the topic.
   * @param conn The active database connection.
   * @return A {@link List} of publication IDs.
   */
  public List<Integer> findPublicationIdsByTopic(int topicId, Connection conn) {
    return findColumnMany(
        conn,
        "SELECT pub_id FROM publication_topic WHERE topic_id = ?",
        Integer.class,
        topicId
    );
  }

  public Map<Integer, List<Topic>> getTopicsMap(List<Integer> pubIds) {
    return withConnection(conn -> getTopicsMap(pubIds, conn));
  }


  public Map<Integer, List<Topic>> getTopicsMap(List<Integer> pubIds, Connection conn) {
    if (pubIds.isEmpty()) return Map.of();

    String placeholders = pubIds.stream().map(id -> "?").collect(Collectors.joining(", "));
    String sql = """
    SELECT pt.pub_id, t.topic_id, t.code, t.full_name
    FROM publication_topic pt
    JOIN topic t ON pt.topic_id = t.topic_id
    WHERE pt.pub_id IN (""" + placeholders + ")";

    List<Map.Entry<Integer, Topic>> entries =
        findMany(conn, sql, rs -> Map.entry(rs.getInt("pub_id"),
                new Topic(rs.getInt("topic_id"), rs.getString("code"), rs.getString("full_name"))),
            pubIds.toArray());

    return entries.stream().collect(Collectors.groupingBy(
        Map.Entry::getKey,
        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
    ));
  }


  /**
   * Maps a row from the 'publication_topic' table to a {@link PublicationTopic} object.
   *
   * @param rs The ResultSet to map from.
   * @return The mapped PublicationTopic object.
   * @throws SQLException if a database access error occurs.
   */
  private PublicationTopic mapRowToPublicationTopic(ResultSet rs) throws SQLException {
    return new PublicationTopic(
        rs.getInt("pub_id"),
        rs.getInt("topic_id")
    );
  }
}