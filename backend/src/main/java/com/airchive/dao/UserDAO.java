package com.airchive.dao;

import com.airchive.model.User;
import com.airchive.model.User.UserRole;
import com.airchive.model.User.UserStatus;
import com.airchive.util.ApplicationContextProvider;
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
 * DAO class for managing operations related to the {@link User} entity.
 * Provides methods to perform CRUD functionalities for the user table in the database.
 */
public class UserDAO {
  private static final Logger logger = Logger.getLogger(UserDAO.class.getName());
  private final DataSource dataSource;

  public UserDAO() {
    ServletContext context = ApplicationContextProvider.getServletContext();
    this.dataSource = (DataSource) context.getAttribute("dataSource");
  }

  /**
   * Creates a new user record in the database based on the provided User object.
   * The user must be a new instance (not yet persisted in the database).
   *
   * @param user the User object containing the details to be persisted in the database
   * @return the User object after a successful insertion, including the generated user ID
   * @throws SQLException if a database access error occurs or the creation operation fails
   * @throws IllegalArgumentException if the user is not new (already persisted in the database)
   */
  public User create(User user) throws SQLException {
    if (!user.isNew()) {
      throw new IllegalArgumentException("User is already in database");
    }
    String sql = "INSERT INTO user (username, first_name, last_name, email, password_hash, role, "
        + "status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

      stmt.setString(1, user.getUsername());
      stmt.setString(2, user.getFirstName());
      stmt.setString(3, user.getLastName());
      stmt.setString(4, user.getEmail());
      stmt.setString(5, user.getPasswordHash());
      stmt.setString(6, user.getRole().name());
      stmt.setString(7, user.getStatus().name());
      stmt.setTimestamp(8, Timestamp.valueOf(user.getCreatedAt()));

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
   * Updates an existing user in the database with the provided user details.
   * Ensures the user has a valid ID before proceeding.
   *
   * @param user the User object containing updated details to be stored in the database
   * @return the User object after a successful update
   * @throws SQLException if a database access error occurs or the update fails
   * @throws IllegalArgumentException if the user is null or does not have a valid ID
   */
  public User update(User user) throws SQLException {
    if (user.isNew()) {
      throw new IllegalArgumentException("User is null or does not exist in database");
    }

    String sql = "UPDATE user SET username = ?, first_name = ?, last_name = ?, email = ?, "
        + "password_hash = ?, role = ?, status = ? WHERE user_id = ?";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, user.getUsername());
      stmt.setString(2, user.getFirstName());
      stmt.setString(3, user.getLastName());
      stmt.setString(4, user.getEmail());
      stmt.setString(5, user.getPasswordHash());
      stmt.setString(6, user.getRole().name());
      stmt.setString(7, user.getStatus().name());
      stmt.setInt(8, user.getUserId());

      int rowsAffected = stmt.executeUpdate();
      if (rowsAffected == 0) {
        throw new SQLException("Updating user failed, no rows affected.");
      }

      logger.info("User updated successfully: " + user.getUsername());
      return user;
    }
  }

  /**
   * Deletes a user from the database based on their unique identifier.
   *
   * @param userId the unique identifier of the user to be deleted
   * @return true if the user was successfully deleted, false otherwise
   * @throws SQLException if any SQL error occurs while executing the delete operation
   */
  public boolean delete(int userId) throws SQLException {
    String sql = "DELETE FROM user WHERE user_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);
      return stmt.executeUpdate() > 0;
    }
  }

  /**
   * Retrieves a user from the database based on their unique identifier.
   *
   * @param userId the unique identifier of the user to be retrieved
   * @return an Optional containing the User if a user with the given ID is found,
   *         or an empty Optional if no such user exists
   */
  public Optional<User> findById(Integer userId) {
    String sql = "SELECT * FROM user WHERE user_id = ?";
    return getUserByField(sql, userId.toString());
  }

  /**
   * Retrieves a user from the database based on their username.
   *
   * @param username the username of the user to be retrieved
   * @return an Optional containing the User if a user with the given username is found,
   *         or an empty Optional if no such user exists
   */
  public Optional<User> findByUsername(String username) {
    String sql = "SELECT * FROM user WHERE username = ?";
    return getUserByField(sql, username);
  }

  /**
   * Retrieves a user from the database based on their email address.
   *
   * @param email the email address of the user to be retrieved
   * @return an Optional containing the User if a user with the given email address is found,
   *         or an empty Optional if no such user exists
   */
  public Optional<User> findByEmail(String email) {
    String sql = "SELECT * FROM user WHERE email = ?";
    return getUserByField(sql, email);
  }

  /**
   * Checks if a username exists in the database.
   *
   * @param username the username to check for existence
   * @return true if the username exists in the database, false otherwise
   */
  public boolean existsByUsername(String username) {
    String sql = "SELECT EXISTS(SELECT 1 FROM user WHERE username = ?)";
    return checkExistence(sql, username);
  }

  /**
   * Checks if an email address exists in the database.
   *
   * @param email the email address to check for existence
   * @return true if the email exists in the database, false otherwise
   */
  public boolean existsByEmail(String email) {
    String sql = "SELECT EXISTS(SELECT 1 FROM user WHERE email = ?)";
    return checkExistence(sql, email);
  }

  // Private helper methods

  /**
   * Retrieves a user from the database based on a specific field and its value.
   *
   * @param sql the SQL query string to execute, expecting a result that maps to a User
   * @param param the parameter value to be used in the SQL query for filtering results
   * @return an Optional containing the User if found, or an empty Optional if not found
   */
  private Optional<User> getUserByField(String sql, String param) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, param);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRow(rs));
        }
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error querying user by field", e);
    }
    return Optional.empty();
  }

  /**
   * Checks the existence of a record in the database based on the provided SQL query and parameter.
   *
   * @param sql the SQL query to execute, which should return a single boolean column indicating existence
   * @param param the parameter value to be used in the SQL query
   * @return true if the record exists, false otherwise
   */
  private boolean checkExistence(String sql, String param) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, param);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getBoolean(1);
        }
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error checking existence by field", e);
    }
    return false;
  }

  /**
   * Maps a row from the given ResultSet to a User object.
   *
   * @param rs the ResultSet containing the row data to be mapped
   * @return a User object populated with data from the current row of the ResultSet
   * @throws SQLException if an SQL error occurs while accessing the ResultSet
   */
  private User mapRow(ResultSet rs) throws SQLException {
    return new User(
        rs.getInt("user_id"),
        rs.getString("username"),
        rs.getString("first_name"),
        rs.getString("last_name"),
        rs.getString("email"),
        rs.getString("password_hash"),
        UserRole.valueOf(rs.getString("role")),
        UserStatus.valueOf(rs.getString("status")),
        rs.getTimestamp("created_at").toLocalDateTime()
    );
  }
}