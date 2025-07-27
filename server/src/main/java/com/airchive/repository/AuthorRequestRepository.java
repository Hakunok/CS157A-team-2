package com.airchive.repository;


import com.airchive.entity.AuthorRequest;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Manages data persistence for {@link AuthorRequest} entities.
 * This repository handles the workflow for users requesting an upgrade to the
 * 'AUTHOR' role. It includes operations for creating, finding, updating, and
 * counting requests, with a focus on handling pending requests for administrative review.
 */
public class AuthorRequestRepository extends BaseRepository {

  /**
   * Creates a new author request in the database.
   * This method manages its own database connection.
   *
   * @param request The AuthorRequest object to persist.
   * @return The created AuthorRequest, retrieved from the database.
   * @throws ValidationException if a request for this account already exists.
   */
  public AuthorRequest create(AuthorRequest request) {
    return withConnection(conn -> create(request, conn));
  }

  /**
   * Creates a new author request using a provided database connection.
   *
   * @param request The AuthorRequest object to persist.
   * @param conn The active database connection.
   * @return The created AuthorRequest, retrieved from the database.
   * @throws ValidationException if a request for this account already exists.
   * @throws EntityNotFoundException if the creation fails and the new request cannot be retrieved.
   */
  public AuthorRequest create(AuthorRequest request, Connection conn) {
    if (existsByAccountId(request.accountId(), conn)) {
      throw new ValidationException("An author request for this account already exists.");
    }

    executeInsertWithGeneratedKey(
        conn,
        "INSERT INTO author_request (account_id, status, requested_at) VALUES (?, ?, NOW())",
        request.accountId(),
        request.status().name()
    );

    return findByAccountId(request.accountId(), conn)
        .orElseThrow(() -> new EntityNotFoundException("Failed to retrieve created author request."));
  }

  /**
   * Finds an author request by the associated account ID.
   *
   * @param accountId The ID of the account.
   * @return An {@link Optional} containing the AuthorRequest, or empty if not found.
   */
  public Optional<AuthorRequest> findByAccountId(int accountId) {
    return withConnection(conn -> findByAccountId(accountId, conn));
  }

  /**
   * Finds an author request by account ID using a provided connection.
   *
   * @param accountId The ID of the account.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the AuthorRequest, or empty if not found.
   */
  public Optional<AuthorRequest> findByAccountId(int accountId, Connection conn) {
    return findOne(conn, "SELECT * FROM author_request WHERE account_id = ?", this::mapRowToRequest, accountId);
  }

  /**
   * Retrieves a paginated list of all pending author requests.
   * This is intended for administrative use to review open requests.
   *
   * @param page The page number (1-indexed).
   * @param pageSize The number of requests per page.
   * @return A {@link List} of pending AuthorRequests.
   */
  public List<AuthorRequest> findAllPending(int page, int pageSize) {
    return withConnection(conn -> findAllPending(page, pageSize, conn));
  }

  /**
   * Retrieves a paginated list of all pending author requests using a provided connection.
   *
   * @param page The page number (1-indexed).
   * @param pageSize The number of requests per page.
   * @param conn The active database connection.
   * @return A {@link List} of pending AuthorRequests.
   */
  public List<AuthorRequest> findAllPending(int page, int pageSize, Connection conn) {
    int offset = (page - 1) * pageSize;
    return findMany(
        conn,
        "SELECT * FROM author_request WHERE status = 'PENDING' ORDER BY requested_at ASC LIMIT ? OFFSET ?",
        this::mapRowToRequest,
        pageSize,
        offset
    );
  }

  /**
   * Counts the total number of pending author requests.
   * Used for pagination.
   *
   * @return The total count of pending requests.
   */
  public int countPending() {
    return withConnection(this::countPending);
  }

  /**
   * Counts the total number of pending author requests using a provided connection.
   *
   * @param conn The active database connection.
   * @return The total count of pending requests.
   */
  public int countPending(Connection conn) {
    return findColumnMany(
        conn,
        "SELECT COUNT(*) FROM author_request WHERE status = 'PENDING'",
        Integer.class
    ).stream().findFirst().orElse(0);
  }

  /**
   * Checks if any author request (regardless of status) exists for a given account.
   *
   * @param accountId The ID of the account.
   * @return {@code true} if a request exists, {@code false} otherwise.
   */
  public boolean existsByAccountId(int accountId) {
    return withConnection(conn -> existsByAccountId(accountId, conn));
  }

  /**
   * Checks if any author request exists for an account using a provided connection.
   *
   * @param accountId The ID of the account.
   * @param conn The active database connection.
   * @return {@code true} if a request exists, {@code false} otherwise.
   */
  public boolean existsByAccountId(int accountId, Connection conn) {
    return exists(conn, "SELECT EXISTS(SELECT 1 FROM author_request WHERE account_id = ?)", accountId);
  }

  /**
   * Checks if an account has a request with 'PENDING' status.
   *
   * @param accountId The ID of the account.
   * @return {@code true} if a pending request exists, {@code false} otherwise.
   */
  public boolean hasPendingRequest(int accountId) {
    return withConnection(conn -> hasPendingRequest(accountId, conn));
  }

  /**
   * Checks for a pending request for an account using a provided connection.
   *
   * @param accountId The ID of the account.
   * @param conn The active database connection.
   * @return {@code true} if a pending request exists, {@code false} otherwise.
   */
  public boolean hasPendingRequest(int accountId, Connection conn) {
    return exists(
        conn,
        "SELECT EXISTS(SELECT 1 FROM author_request WHERE account_id = ? AND status = 'PENDING')",
        accountId
    );
  }

  /**
   * Updates the status of an author request (e.g., to 'APPROVED').
   *
   * @param accountId The ID of the account whose request is being updated.
   * @param status The new status for the request.
   * @throws EntityNotFoundException if no request is found for the given account ID.
   */
  public void updateStatus(int accountId, AuthorRequest.Status status) {
    withConnection(conn -> {
      updateStatus(accountId, status, conn);
      return null;
    });
  }

  /**
   * Updates the status of an author request using a provided connection.
   *
   * @param accountId The ID of the account whose request is being updated.
   * @param status The new status for the request.
   * @param conn The active database connection.
   * @throws EntityNotFoundException if no request is found for the given account ID.
   */
  public void updateStatus(int accountId, AuthorRequest.Status status, Connection conn) {
    int rows = executeUpdate(conn,
        "UPDATE author_request SET status = ? WHERE account_id = ?",
        status.name(), accountId);
    if (rows == 0) {
      throw new EntityNotFoundException("Author request not found for update.");
    }
  }

  /**
   * Maps a row from the 'author_request' table to an {@link AuthorRequest} object.
   *
   * @param rs The ResultSet to map from.
   * @return The mapped AuthorRequest object.
   * @throws SQLException if a database access error occurs.
   */
  private AuthorRequest mapRowToRequest(ResultSet rs) throws SQLException {
    return new AuthorRequest(
        rs.getInt("account_id"),
        AuthorRequest.Status.valueOf(rs.getString("status")),
        rs.getTimestamp("requested_at").toLocalDateTime()
    );
  }
}
