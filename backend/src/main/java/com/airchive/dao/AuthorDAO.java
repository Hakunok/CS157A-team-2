package com.airchive.dao;

import com.airchive.model.Author;
import com.airchive.util.ApplicationContextProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 * A Data Access Object (DAO) for managing `Author` entities in a persistent storage.
 * This class provides methods to perform Create, Read, Update, and Delete (CRUD)
 * operations on the `author` database table.
 */
public class AuthorDAO {
  private final DataSource dataSource;
  private static final Logger logger = Logger.getLogger(AuthorDAO.class.getName());

  public AuthorDAO() {
    ServletContext context = ApplicationContextProvider.getServletContext();
    this.dataSource = (DataSource) context.getAttribute("dataSource");
  }

  /**
   * Creates a new Author entry in the database.
   *
   * @param author the Author object containing the details of the author to be created
   * @return the created Author object with its generated ID
   * @throws SQLException if a database access error occurs or the author could not be created
   */
  public Author create(Author author) throws SQLException {
    String sql = "INSERT INTO author (user_id, first_name, last_name, bio, created_at, is_user) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      stmt.setObject(1, author.getUserId(), Types.INTEGER);
      stmt.setString(2, author.getFirstName());
      stmt.setString(3, author.getLastName());
      stmt.setString(4, author.getBio());
      stmt.setTimestamp(5, Timestamp.valueOf(author.getCreatedAt()));
      stmt.setBoolean(6, author.isUser());

      int rows = stmt.executeUpdate();
      if (rows == 0) {
        throw new SQLException("Creating author failed, no rows affected.");
      }

      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          author.setAuthorId(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Creating author failed, no ID obtained.");
        }
      }

      return author;
    }
  }

  /**
   * Retrieves an Author by its unique identifier.
   *
   * @param authorId the unique identifier of the author to retrieve
   * @return an Optional containing the found Author if it exists, or an empty Optional if no author
   *         was found with the given ID
   * @throws SQLException if a database access error occurs
   */
  public Optional<Author> findById(int authorId) throws SQLException {
    String sql = "SELECT * FROM author WHERE author_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, authorId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRow(rs));
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Finds an Author by the specified userId.
   *
   * @param userId the unique identifier of the user to find the Author for
   * @return an Optional containing the found Author if it exists, or an empty Optional if no Author
   *         is found for the given userId
   * @throws SQLException if a database access error occurs while retrieving the Author
   */
  public Optional<Author> findByUserId(int userId) throws SQLException {
    String sql = "SELECT * FROM author WHERE user_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRow(rs));
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Retrieves all authors from the database, ordered by their last name and first name.
   *
   * @return a list of authors with all available author records; returns an empty list if no records are found
   * @throws SQLException if a database access error occurs
   */
  public List<Author> findAll() throws SQLException {
    String sql = "SELECT * FROM author ORDER BY last_name, first_name";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      List<Author> authors = new ArrayList<>();
      while (rs.next()) {
        authors.add(mapRow(rs));
      }
      return authors;
    }
  }

  /**
   * Updates the details of an existing author in the database.
   * The author's ID must be specified for the update to be performed.
   *
   * @param author the Author object containing the updated details, including a valid author ID
   * @return the updated Author object
   * @throws IllegalArgumentException if the author's ID is null
   * @throws SQLException if a database access error occurs or the update fails
   */
  public Author update(Author author) throws SQLException {
    if (author.getAuthorId() == null) {
      throw new IllegalArgumentException("Cannot update author without ID.");
    }

    String sql = "UPDATE author SET user_id = ?, first_name = ?, last_name = ?, bio = ?, is_user = ? WHERE author_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setObject(1, author.getUserId(), Types.INTEGER);
      stmt.setString(2, author.getFirstName());
      stmt.setString(3, author.getLastName());
      stmt.setString(4, author.getBio());
      stmt.setBoolean(5, author.isUser());
      stmt.setInt(6, author.getAuthorId());

      int rows = stmt.executeUpdate();
      if (rows == 0) {
        throw new SQLException("Updating author failed, no rows affected.");
      }

      return author;
    }
  }

  /**
   * Deletes an author from the database based on the provided author ID.
   *
   * @param authorId the unique identifier of the author to be deleted
   * @return true if the author was successfully deleted, false otherwise
   * @throws SQLException if a database access error occurs
   */
  public boolean delete(Integer authorId) throws SQLException {
    String sql = "DELETE FROM author WHERE author_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, authorId);
      return stmt.executeUpdate() > 0;
    }
  }

  /**
   * Maps a row from the provided ResultSet to an Author object.
   *
   * @param rs the ResultSet object containing the row data to map
   * @return an Author object instantiated with the data from the current row in the ResultSet
   * @throws SQLException if a database access error occurs while reading the ResultSet
   */
  private Author mapRow(ResultSet rs) throws SQLException {
    return new Author(
        rs.getInt("author_id"),
        (Integer) rs.getObject("user_id"),
        rs.getString("first_name"),
        rs.getString("last_name"),
        rs.getString("bio"),
        rs.getBoolean("is_user"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );
  }
}

