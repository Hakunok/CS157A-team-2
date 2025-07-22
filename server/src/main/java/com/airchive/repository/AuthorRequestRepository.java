package com.airchive.repository;


import com.airchive.entity.AuthorRequest;
import com.airchive.exception.EntityNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AuthorRequestRepository extends BaseRepository {

  public AuthorRequest create(int userId) {
    return executeWithConnection(conn -> create(userId, conn));
  }

  public AuthorRequest create(int userId, Connection conn) {
    int newId = executeInsertWithGeneratedKey(
        conn,
        "INSERT INTO author_request (user_id) VALUES (?)",
        userId
    );
    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Failed to create author request."));
  }

  public void updateStatus(int requestId, AuthorRequest.Status newStatus) {
    executeWithConnection(conn -> {
      updateStatus(requestId, newStatus, conn);
      return null;
    });
  }

  public void updateStatus(int requestId, AuthorRequest.Status newStatus, Connection conn) {
    int rowsAffected = executeUpdate(
        conn,
        "UPDATE author_request SET status = ?, decided_at = NOW() WHERE request_id = ?",
        newStatus.name(),
        requestId
    );
    if (rowsAffected == 0) {
      throw new EntityNotFoundException("Update status failed for request id: " + requestId);
    }
  }

  public Optional<AuthorRequest> findById(int requestId) {
    return executeWithConnection(conn -> findById(requestId, conn));
  }

  public Optional<AuthorRequest> findById(int requestId, Connection conn) {
    return findOne(conn, "SELECT * FROM author_request WHERE request_id = ?", this::mapRowToRequest, requestId);
  }

  public Optional<AuthorRequest> findByUserId(int userId) {
    return executeWithConnection(conn -> findByUserId(userId, conn));
  }

  public Optional<AuthorRequest> findByUserId(int userId, Connection conn) {
    return findOne(conn, "SELECT * FROM author_request WHERE user_id = ?", this::mapRowToRequest, userId);
  }

  public List<AuthorRequest> findPending() {
    return executeWithConnection(this::findPending);
  }

  public List<AuthorRequest> findPending(Connection conn) {
    return findMany(conn, "SELECT * FROM author_request WHERE status = 'PENDING' ORDER BY requested_at ASC", this::mapRowToRequest);
  }

  public boolean hasPendingRequest(int userId) {
    return executeWithConnection(conn -> hasPendingRequest(userId, conn));
  }

  public boolean hasPendingRequest(int userId, Connection conn) {
    return exists(
        conn,
        "SELECT EXISTS(SELECT 1 FROM author_request WHERE user_id = ? AND status = 'PENDING')",
        userId
    );
  }

  private AuthorRequest mapRowToRequest(ResultSet rs) throws SQLException {
    return new AuthorRequest(
        rs.getInt("request_id"),
        rs.getInt("user_id"),
        AuthorRequest.Status.valueOf(rs.getString("status")),
        rs.getObject("requested_at", LocalDateTime.class),
        rs.getObject("decided_at", LocalDateTime.class)
    );
  }
}
