package com.airchive.repository;

import com.airchive.entity.Topic;
import com.airchive.exception.EntityNotFoundException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TopicRepository extends BaseRepository {

  public List<Topic> findAll() {
    return executeWithConnection(conn ->
        findMany(conn, "SELECT * FROM topic ORDER BY full_name ASC", this::mapRowToTopic)
    );
  }

  public Optional<Topic> findById(int topicId) {
    return executeWithConnection(conn ->
        findById(topicId, conn)
    );
  }

  public Optional<Topic> findById(int topicId, Connection conn) {
    return findOne(conn, "SELECT * FROM topic WHERE topic_id = ?", this::mapRowToTopic, topicId);
  }

  public Optional<Topic> findByCode(String code) {
    return executeWithConnection(conn ->
        findOne(conn, "SELECT * FROM topic WHERE code = ?", this::mapRowToTopic, code)
    );
  }

  public List<Topic> search(String query) {
    return executeWithConnection(conn ->
        findMany(
            conn,
            "SELECT * FROM topic WHERE full_name LIKE ? OR code LIKE ? ORDER BY full_name ASC",
            this::mapRowToTopic,
            "%" + query + "%",
            "%" + query + "%"
        )
    );
  }

  public Topic create(String code, String fullName, String colorHex) {
    return executeWithConnection(conn -> {
      int newId = executeInsertWithGeneratedKey(
          conn,
          "INSERT INTO topic (code, full_name, color_hex) VALUES (?, ?, ?)",
          code,
          fullName,
          colorHex
      );
      return findById(newId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Failed to create topic."));
    });
  }

  public Topic update(int topicId, String code, String fullName, String colorHex) {
    return executeWithConnection(conn -> {
      int rowsAffected = executeUpdate(
          conn,
          "UPDATE topic SET code = ?, full_name = ?, color_hex = ? WHERE topic_id = ?",
          code,
          fullName,
          colorHex,
          topicId
      );

      if (rowsAffected == 0) {
        throw new EntityNotFoundException("Topic not found: " + topicId);
      }

      return findById(topicId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Failed to reload updated topic."));
    });
  }

  private Topic mapRowToTopic(ResultSet rs) throws SQLException {
    return new Topic(
        rs.getInt("topic_id"),
        rs.getString("code"),
        rs.getString("full_name"),
        rs.getString("color_hex")
    );
  }
}
