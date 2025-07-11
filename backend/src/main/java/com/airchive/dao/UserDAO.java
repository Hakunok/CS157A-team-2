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
      "INSERT INTO user (username, first_name, last_name, email, password_hash, role, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String SELECT_USER_BY_ID =
      "SELECT * FROM user WHERE user_id = ?";

  private static final String SELECT_USER_BY_USERNAME =
      "SELECT * FROM user WHERE username = ?";

  private static final String SELECT_USER_BY_EMAIL =
      "SELECT * FROM user WHERE email = ?";

  private static final String UPDATE_USER =
      "UPDATE user SET username = ?, first_name = ?, last_name = ?, email = ?, password_hash = ?, role = ?, status = ? WHERE user_id = ?";

  private static final String DELETE_USER =
      "DELETE FROM user WHERE user_id = ?";

  private static final String EXISTS_BY_USERNAME =
      "SELECT EXISTS(SELECT 1 FROM user WHERE username = ?)";

  private static final String EXISTS_BY_EMAIL =
      "SELECT EXISTS(SELECT 1 FROM user WHERE email = ?)";


  public UserDAO(DataSource dataSource) {
    this.dataSource = dataSource;
    if (this.dataSource == null) {
      throw new IllegalStateException("DataSource cannot be null");
    }
  }

  public UserDAO(ServletContext context) {
    this((DataSource) context.getAttribute("dataSource"));
  }

  public User create(User user) throws SQLException {
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

  public User update(User user) throws SQLException {
    if (user.isNew()) {
      throw new IllegalArgumentException("User is null or does not exist in database");
    }

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(UPDATE_USER)) {

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

  public Optional<User> findById(Integer userId) {
    return getUserByField(SELECT_USER_BY_ID, userId.toString());
  }

  public Optional<User> findByUsername(String username) {
    return getUserByField(SELECT_USER_BY_USERNAME, username);
  }

  public Optional<User> findByEmail(String email) {
    return getUserByField(SELECT_USER_BY_EMAIL, email);
  }

  public boolean existsByUsername(String username) {
    return checkExistence(EXISTS_BY_USERNAME, username);
  }

  public boolean existsByEmail(String email) {
    return checkExistence(EXISTS_BY_EMAIL, email);
  }

  // Private helper methods

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

  private User extractUser(ResultSet rs) throws SQLException {
    User user = new User();
    user.setUserId(rs.getInt("user_id"));
    user.setUsername(rs.getString("username"));
    user.setFirstName(rs.getString("first_name"));
    user.setLastName(rs.getString("last_name"));
    user.setEmail(rs.getString("email"));
    user.setPasswordHash(rs.getString("password_hash"));
    user.setRole(User.Role.valueOf(rs.getString("role")));
    user.setStatus(User.Status.valueOf(rs.getString("status")));
    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    return user;
  }
}