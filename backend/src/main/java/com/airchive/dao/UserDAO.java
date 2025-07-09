package com.airchive.dao;

import com.airchive.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 * UserDAO is responsible for performing CRUD operations on the "User" table in the database.
 * It provides methods to save, update, and retrieve User records, as well as utility
 * methods to check for the existence of usernames or emails.
 */
public class UserDAO {
  private static final Logger logger = Logger.getLogger(UserDAO.class.getName());
  private final DataSource dataSource;

  // SQL Constants
  private static final String INSERT_USER =
      "INSERT INTO User (username, first_name, last_name, email, password_hash, created_at) VALUES (?, ?, ?, ?, ?, ?)";

  private static final String SELECT_USER_BY_ID =
      "SELECT * FROM User WHERE user_id = ?";

  private static final String SELECT_USER_BY_USERNAME =
      "SELECT * FROM User WHERE username = ?";

  private static final String SELECT_USER_BY_EMAIL =
      "SELECT * FROM User WHERE email = ?";

  private static final String UPDATE_USER =
      "UPDATE User SET username = ?, first_name = ?, last_name = ?, email = ?, password_hash = ? WHERE user_id = ?";

  private static final String DELETE_USER =
      "DELETE FROM User WHERE user_id = ?";

  private static final String COUNT_BY_USERNAME =
      "SELECT COUNT(*) FROM User WHERE username = ?";

  private static final String COUNT_BY_EMAIL =
      "SELECT COUNT(*) FROM User WHERE email = ?";


  public UserDAO(DataSource dataSource) {
    this.dataSource = dataSource;
    if (this.dataSource == null) {
      throw new IllegalStateException("DataSource cannot be null");
    }
  }


  public UserDAO(ServletContext context) {
    this((DataSource) context.getAttribute("dataSource"));
  }

  /**
   * Saves a new User to the database. The User must not already exist in the database.
   *
   * @param user the User object to save. The User must not be null, and its ID must be null (indicating it is new).
   * @return the saved User object with the database-generated ID assigned.
   * @throws IllegalArgumentException if the User is null or already exists in the database.
   * @throws SQLException if any database operation fails during the save process.
   */
  public User save(User user) throws SQLException {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    if (!user.isNew()) {
      throw new IllegalArgumentException("User already exists in database");
    }

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(INSERT_USER, PreparedStatement.RETURN_GENERATED_KEYS)) {

      stmt.setString(1, user.getUsername());
      stmt.setString(2, user.getFirstName());
      stmt.setString(3, user.getLastName());
      stmt.setString(4, user.getEmail());
      stmt.setString(5, user.getPasswordHash());
      stmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));

      int rowsAffected = stmt.executeUpdate();
      if (rowsAffected == 0) {
        throw new SQLException("Creating user failed, no rows affected.");
      }

      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          user.setUserId(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Creating user failed, no ID obtained.");
        }
      }

      logger.info("User created successfully: " + user.getUsername());
      return user;
    }
  }

  /**
   * Updates an existing User in the database with the provided information.
   *
   * @param user the User object containing updated information. The User must not be null and must already exist in the database.
   * @return the updated User object with the changes persistently applied.
   * @throws IllegalArgumentException if the provided User is null or if the User does not exist in the database.
   * @throws SQLException if an error occurs during the database update operation.
   */
  public User update(User user) throws SQLException {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    if (user.isNew()) {
      throw new IllegalArgumentException("User does not exist in database");
    }

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(UPDATE_USER)) {

      stmt.setString(1, user.getUsername());
      stmt.setString(2, user.getFirstName());
      stmt.setString(3, user.getLastName());
      stmt.setString(4, user.getEmail());
      stmt.setString(5, user.getPasswordHash());
      stmt.setInt(6, user.getUserId());

      int rowsAffected = stmt.executeUpdate();
      if (rowsAffected == 0) {
        throw new SQLException("Updating user failed, no rows affected.");
      }

      logger.info("User updated successfully: " + user.getUsername());
      return user;
    }
  }

  /**
   * Finds a User by their unique ID.
   *
   * @param userId the unique identifier of the User to find (must not be null)
   * @return an Optional containing the User if found, or Optional.empty() if no User is found with the given ID
   */
  public Optional<User> findById(Integer userId) {
    if (userId == null) {
      return Optional.empty();
    }
    return getUserByField(SELECT_USER_BY_ID, userId.toString());
  }

  /**
   * Finds a User by their unique username.
   *
   * @param username the username of the User to find (must not be null or empty)
   * @return an Optional containing the User if found, or Optional.empty() if no User is found with the given username
   */
  public Optional<User> findByUsername(String username) {
    if (username == null || username.isEmpty()) {
      return Optional.empty();
    }
    return getUserByField(SELECT_USER_BY_USERNAME, username);
  }

  /**
   * Finds a User in the database by their unique email address.
   *
   * @param email the email address of the User to find (must not be null or empty)
   * @return an Optional containing the User if found, or Optional.empty() if no User is found with the given email
   */
  public Optional<User> findByEmail(String email) {
    if (email == null || email.isEmpty()) {
      return Optional.empty();
    }
    return getUserByField(SELECT_USER_BY_EMAIL, email);
  }

  /**
   * Checks if a username already exists in the database.
   *
   * @param username the username to check for existence (must not be null or empty)
   * @return true if the username exists in the database, false otherwise
   */
  public boolean existsByUsername(String username) {
    if (username == null || username.isEmpty()) {
      return false;
    }
    return countByField(COUNT_BY_USERNAME, username) > 0;
  }

  /**
   * Checks if an email address already exists in the database.
   *
   * @param email the email address to check for existence (must not be null or empty)
   * @return true if the email exists in the database, false otherwise
   */
  public boolean existsByEmail(String email) {
    if (email == null || email.isEmpty()) {
      return false;
    }
    return countByField(COUNT_BY_EMAIL, email) > 0;
  }

  // --- Private helper methods ---

  private Optional<User> getUserByField(String sql, String param) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, param);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(extractUser(rs));
        }
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error querying user by field", e);
    }
    return Optional.empty();
  }

  private int countByField(String sql, String param) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, param);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error counting by field", e);
    }
    return 0;
  }

  private User extractUser(ResultSet rs) throws SQLException {
    User user = new User();
    user.setUserId(rs.getInt("user_id"));
    user.setUsername(rs.getString("username"));
    user.setFirstName(rs.getString("first_name"));
    user.setLastName(rs.getString("last_name"));
    user.setEmail(rs.getString("email"));
    user.setPasswordHash(rs.getString("password_hash"));
    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    return user;
  }
}