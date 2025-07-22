package com.airchive.repository;

import com.airchive.entity.User;

import com.airchive.exception.EntityNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepository {

  public User create(User user) {
    return executeWithConnection(conn -> create(user, conn));
  }

  public User create(User user, Connection conn) {
    int newId = executeInsertWithGeneratedKey(
        conn,
        "INSERT INTO user (username, first_name, last_name, email, password_hash) VALUES (?, ?, ?, ?, ?)",
        user.username(),
        user.firstName(),
        user.lastName(),
        user.email(),
        user.passwordHash()
    );

    return this.findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Failed to create user"));
  }

  public void updatePermission(int userId, User.Permission permission) {
    executeWithConnection(conn -> {
      updatePermission(userId, permission, conn);
      return null;
    });
  }

  public void updatePermission(int userId, User.Permission permission, Connection conn) {
    int rowsAffected = executeUpdate(
        conn,
        "UPDATE user SET permission = ? WHERE user_id = ?",
        permission.name(),
        userId
    );
    if (rowsAffected == 0) {
      throw new EntityNotFoundException("Update permission failed for user id: " + userId);
    }
  }

  public void updateStatus(int userId, User.Status status) {
    executeWithConnection(conn -> {
      updateStatus(userId, status, conn);
      return null;
    });
  }

  public void updateStatus(int userId, User.Status status, Connection conn) {
    int rowsAffected = executeUpdate(
        conn,
        "UPDATE user SET status = ? WHERE user_id = ?",
        status.name(),
        userId
    );
    if (rowsAffected == 0) {
      throw new EntityNotFoundException("Update status failed for user id: " + userId);
    }
  }

  public void updateUsername(int userId, String username) {
    executeWithConnection(conn -> {
      updateUsername(userId, username, conn);
      return null;
    });
  }

  public void updateUsername(int userId, String username, Connection conn) {
    int rowsAffected = executeUpdate(
        conn,
        "UPDATE user SET username = ? WHERE user_id = ?",
        username,
        userId
    );
    if (rowsAffected == 0) {
      throw new EntityNotFoundException("Update username failed for user id: " + userId);
    }
  }

  public void updateFirstName(int userId, String firstName) {
    executeWithConnection(conn -> {
      updateFirstName(userId, firstName, conn);
      return null;
    });
  }

  public void updateFirstName(int userId, String firstName, Connection conn) {
    int rowsAffected = executeUpdate(
        conn,
        "UPDATE user SET first_name = ? WHERE user_id = ?",
        firstName,
        userId
    );
    if (rowsAffected == 0) {
      throw new EntityNotFoundException("Update first name failed for user id: " + userId);
    }
  }

  public void updateLastName(int userId, String lastName) {
    executeWithConnection(conn -> {
      updateLastName(userId, lastName, conn);
      return null;
    });
  }

  public void updateLastName(int userId, String lastName, Connection conn) {
    int rowsAffected = executeUpdate(
        conn,
        "UPDATE user SET last_name = ? WHERE user_id = ?",
        lastName,
        userId
    );
    if (rowsAffected == 0) {
      throw new EntityNotFoundException("Update last name failed for user id: " + userId);
    }
  }

  public void updatePasswordHash(int userId, String passwordHash) {
    executeWithConnection(conn -> {
      updateLastName(userId, passwordHash, conn);
      return null;
    });
  }

  public void updatePasswordHash(int userId, String passwordHash, Connection conn) {
    int rowsAffected = executeUpdate(
        conn,
        "UPDATE user SET password_hash = ? WHERE user_id = ?",
        passwordHash,
        userId
    );
    if (rowsAffected == 0) {
      throw new EntityNotFoundException("Update password failed for user id: " + userId);
    }
  }

  public Optional<User> findById(int userId) {
    return executeWithConnection(conn -> findById(userId, conn));
  }

  public Optional<User> findById(int userId, Connection conn) {
    return findOne(conn, "SELECT * FROM user WHERE user_id = ?", this::mapRowToUser, userId);
  }

  public Optional<User> findByUsername(String username) {
    return executeWithConnection(conn -> findByUsername(username, conn));
  }

  public Optional<User> findByUsername(String username, Connection conn) {
    return findOne(conn, "SELECT * FROM user WHERE username = ?", this::mapRowToUser, username);
  }

  public Optional<User> findByEmail(String email) {
    return executeWithConnection(conn -> findByEmail(email, conn));
  }

  public Optional<User> findByEmail(String email, Connection conn) {
    return findOne(conn, "SELECT * FROM user WHERE email = ?", this::mapRowToUser, email);
  }

  public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
    return executeWithConnection(conn -> findByUsernameOrEmail(usernameOrEmail, conn));
  }

  public Optional<User> findByUsernameOrEmail(String usernameOrEmail, Connection conn) {
    return findOne(conn, "SELECT * FROM user WHERE username = ? OR email = ?", this::mapRowToUser, usernameOrEmail, usernameOrEmail);
  }

  public boolean existsByUsername(String username) {
    return executeWithConnection(conn -> existsByUsername(username, conn));
  }

  public boolean existsByUsername(String username, Connection conn) {
    return exists(conn, "SELECT EXISTS(SELECT 1 FROM user WHERE username = ?)", username);
  }

  public boolean existsByEmail(String email) {
    return executeWithConnection(conn -> existsByEmail(email, conn));
  }

  public boolean existsByEmail(String email, Connection conn) {
    return exists(conn, "SELECT EXISTS(SELECT 1 FROM user WHERE email = ?)", email);
  }

  public List<User> findAll(int pageNumber, int pageSize) {
    return executeWithConnection(conn -> findAll(pageNumber, pageSize, conn));
  }

  public List<User> findAll(int pageNumber, int pageSize, Connection conn) {
    int offset = (pageNumber - 1) * pageSize;
    return findMany(conn, "SELECT * FROM user ORDER BY user_id ASC LIMIT ? OFFSET ?", this::mapRowToUser, pageSize, offset);
  }

  private User mapRowToUser(ResultSet rs) throws SQLException {
    return new User(
        rs.getInt("user_id"),
        rs.getString("username"),
        rs.getString("first_name"),
        rs.getString("last_name"),
        rs.getString("email"),
        rs.getString("password_hash"),
        User.Permission.valueOf(rs.getString("permission")),
        User.Status.valueOf(rs.getString("status")),
        rs.getObject("created_at", LocalDateTime.class)
    );
  }
}

