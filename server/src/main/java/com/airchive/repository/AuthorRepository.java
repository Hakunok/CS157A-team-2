package com.airchive.repository;

import com.airchive.entity.Author;
import com.airchive.entity.User;
import com.airchive.exception.EntityNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AuthorRepository extends BaseRepository {

  public Author create(String firstName, String lastName) {
    return executeWithConnection(conn -> create(firstName, lastName, conn));
  }

  public Author create(String firstName, String lastName, Connection conn) {
    int newId = executeInsertWithGeneratedKey(
        conn,
        "INSERT INTO author (first_name, last_name) VALUES (?, ?)",
        firstName,
        lastName
    );
    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Failed to create author."));
  }

  public Author createFromUser(User user) {
    return executeWithConnection(conn -> createFromUser(user, conn));
  }

  public Author createFromUser(User user, Connection conn) {
    int newId = executeInsertWithGeneratedKey(
        conn,
        "INSERT INTO author (user_id, first_name, last_name, is_user) VALUES (?, ?, ?, ?)",
        user.id(),
        user.firstName(),
        user.lastName(),
        true
    );
    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Failed to create author from user."));
  }

  public void linkUserToAuthor(int authorId, User user) {
    executeWithConnection(conn -> {
      linkUserToAuthor(authorId, user, conn);
      return null;
    });
  }

  public void linkUserToAuthor(int authorId, User user, Connection conn) {
    int rowsAffected = executeUpdate(
        conn,
        "UPDATE author SET user_id = ?, is_user = true, first_name = ?, last_name = ? WHERE author_id = ?",
        user.id(),
        user.firstName(),
        user.lastName(),
        authorId
    );
    if (rowsAffected == 0) {
      throw new EntityNotFoundException("Cannot link user to author for non-existent author id: " + authorId);
    }
  }

  public void updateBio(int authorId, String newBio) {
    executeWithConnection(conn -> {
      updateBio(authorId, newBio, conn);
      return null;
    });
  }

  public void updateBio(int authorId, String newBio, Connection conn) {
    int rowsAffected = executeUpdate(
        conn,
        "UPDATE author SET bio = ? WHERE author_id = ?",
        newBio,
        authorId
    );
    if (rowsAffected == 0) {
      throw new EntityNotFoundException("Cannot update bio for non-existent author id: " + authorId);
    }
  }

  public Optional<Author> findById(int authorId) {
    return executeWithConnection(conn -> findById(authorId, conn));
  }

  public Optional<Author> findById(int authorId, Connection conn) {
    return findOne(
        conn,
        "SELECT * FROM author WHERE author_id = ?",
        this::mapRowToAuthor,
        authorId
    );
  }

  public Optional<Author> findByUserId(int userId) {
    return executeWithConnection(conn -> findByUserId(userId, conn));
  }

  public Optional<Author> findByUserId(int userId, Connection conn) {
    return findOne(
        conn,
        "SELECT * FROM author WHERE user_id = ?",
        this::mapRowToAuthor,
        userId
    );
  }

  public List<Author> findAll() {
    return executeWithConnection(this::findAll);
  }

  public List<Author> findAll(Connection conn) {
    return findMany(
        conn,
        "SELECT * FROM author ORDER BY author_id ASC",
        this::mapRowToAuthor
    );
  }


  private Author mapRowToAuthor(ResultSet rs) throws SQLException {
    return new Author(
        rs.getInt("author_id"),
        (Integer) rs.getObject("user_id"),
        rs.getString("first_name"),
        rs.getString("last_name"),
        rs.getString("bio"),
        rs.getBoolean("is_user"),
        rs.getObject("created_at", LocalDateTime.class)
    );
  }
}
