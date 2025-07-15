package com.airchive.dao;

import com.airchive.model.Topic;
import com.airchive.util.ApplicationContextProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 * DAO class for managing operations related to the {@link Topic} entity.
 * Provides methods to perform CRUD functionalities for the topic table in the database.
 */
public class TopicDAO {
  private final DataSource dataSource;

  public TopicDAO() throws SQLException {
    ServletContext context = ApplicationContextProvider.getServletContext();
    this.dataSource = (DataSource) context.getAttribute("dataSource");
  }

  /**
   * Inserts a new topic into the database and updates its ID with the generated key.
   *
   * @param topic the {@link Topic} object to be inserted into the database
   *              containing the code, name, and colorHex.
   * @return the {@link Topic} object after being inserted, with its topic ID set to the generated key.
   * @throws SQLException if a database access error occurs or the SQL statement fails.
   */
  public Topic create(Topic topic) throws SQLException {
    String sql = "INSERT INTO topic (code, name, color_hex) VALUES (?, ?, ?)";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      stmt.setString(1, topic.getCode());
      stmt.setString(2, topic.getName());
      stmt.setString(3, topic.getColorHex());

      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        if (keys.next()) {
          topic.setTopicId(keys.getInt(1));
        }
      }

      return topic;
    }
  }

  /**
   * Retrieves a topic from the database based on its unique identifier.
   *
   * @param id the unique identifier of the topic to retrieve.
   * @return an {@link Optional} containing the {@link Topic} if found, or an empty {@link Optional}
   *         if no topic with the specified ID exists.
   * @throws SQLException if a database access error occurs or the SQL statement fails.
   */
  public Optional<Topic> findById(int id) throws SQLException {
    String sql = "SELECT * FROM topic WHERE topic_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) return Optional.of(mapRow(rs));
    }
    return Optional.empty();
  }

  /**
   * Retrieves a topic from the database based on its unique code.
   *
   * @param code the unique code of the topic to retrieve.
   * @return an {@link Optional} containing the {@link Topic} if found, or an empty {@link Optional}
   *         if no topic with the specified code exists.
   * @throws SQLException if a database access error occurs or the SQL statement fails.
   */
  public Optional<Topic> findByCode(String code) throws SQLException {
    String sql = "SELECT * FROM topic WHERE code = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, code);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) return Optional.of(mapRow(rs));
    }
    return Optional.empty();
  }

  /**
   * Retrieves all topics from the database, ordered by their name.
   *
   * @return a list of {@link Topic} objects representing all topics in the database.
   * @throws SQLException if a database access error occurs or the SQL statement fails.
   */
  public List<Topic> findAll() throws SQLException {
    String sql = "SELECT * FROM topic ORDER BY name";
    List<Topic> list = new ArrayList<>();
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        list.add(mapRow(rs));
      }
    }
    return list;
  }

  /**
   * Searches for topics in the database whose name or code matches the given keyword.
   * The search is case-insensitive and matches partial keywords using LIKE.
   *
   * @param keyword the keyword to search for, used to match against topic names or codes
   * @return a list of {@link Topic} objects that match the given keyword, ordered by name
   * @throws SQLException if a database access error occurs or the SQL statement fails
   */
  public List<Topic> searchByKeyword(String keyword) throws SQLException {
    String sql = "SELECT * FROM topic WHERE LOWER(name) LIKE ? OR LOWER(code) LIKE ? ORDER BY name ASC";
    List<Topic> results = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      String pattern = "%" + keyword.toLowerCase() + "%";
      stmt.setString(1, pattern);
      stmt.setString(2, pattern);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    }

    return results;
  }

  /**
   * Maps a database result set row to a {@link Topic} object.
   *
   * @param rs the {@link ResultSet} containing the row data from the database
   * @return a {@link Topic} object constructed from the result set row
   * @throws SQLException if a database access error occurs or the column labels are invalid
   */
  private Topic mapRow(ResultSet rs) throws SQLException {
    return new Topic(
        rs.getInt("topic_id"),
        rs.getString("code"),
        rs.getString("name"),
        rs.getString("color_hex")
    );
  }
}
