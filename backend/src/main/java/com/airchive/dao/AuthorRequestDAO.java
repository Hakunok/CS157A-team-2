package com.airchive.dao;

import com.airchive.model.AuthorRequest;
import com.airchive.model.AuthorRequest.RequestStatus;
import com.airchive.util.ApplicationContextProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 * DAO class for managing operations related to the {@link AuthorRequest} entity.
 * Provides methods to perform CRUD functionalities for the author_request table in the database.
 */
public class AuthorRequestDAO {
  private final DataSource dataSource;

  public AuthorRequestDAO() {
    ServletContext context = ApplicationContextProvider.getServletContext();
    this.dataSource = (DataSource) context.getAttribute("dataSource");
  }

  public AuthorRequest create(AuthorRequest request) throws SQLException {
    String sql = "INSERT INTO author_request (user_id, status, requested_at, approved_at, "
        + "rejected_at) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      stmt.setInt(1, request.getUserId());
      stmt.setString(2, request.getStatus().name());
      stmt.setTimestamp(3, Timestamp.valueOf(request.getRequestedAt()));
      stmt.setTimestamp(4, request.getApprovedAt() != null ? Timestamp.valueOf(request.getApprovedAt()) : null);
      stmt.setTimestamp(5, request.getRejectedAt() != null ? Timestamp.valueOf(request.getRejectedAt()) : null);

      stmt.executeUpdate();
      ResultSet keys = stmt.getGeneratedKeys();
      if (keys.next()) {
        request.setRequestId(keys.getInt(1));
      }
      return request;
    }
  }

  public Optional<AuthorRequest> findById(int requestId) throws SQLException {
    String sql = "SELECT * FROM author_request WHERE request_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, requestId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapRow(rs));
      }
    }
    return Optional.empty();
  }

  public Optional<AuthorRequest> findByUserId(int userId) throws SQLException {
    String sql = "SELECT * FROM author_request WHERE user_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapRow(rs));
      }
    }
    return Optional.empty();
  }

  public List<AuthorRequest> findAllPending() throws SQLException {
    String sql = "SELECT * FROM author_request WHERE status = 'PENDING' ORDER BY requested_at";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      List<AuthorRequest> list = new ArrayList<>();
      while (rs.next()) {
        list.add(mapRow(rs));
      }
      return list;
    }
  }

  public AuthorRequest update(AuthorRequest request) throws SQLException {
    String sql = "UPDATE author_request SET status = ?, approved_at = ?, rejected_at = ? WHERE request_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, request.getStatus().name());
      stmt.setTimestamp(2, request.getApprovedAt() != null ? Timestamp.valueOf(request.getApprovedAt()) : null);
      stmt.setTimestamp(3, request.getRejectedAt() != null ? Timestamp.valueOf(request.getRejectedAt()) : null);
      stmt.setInt(4, request.getRequestId());

      stmt.executeUpdate();
      return request;
    }
  }

  private AuthorRequest mapRow(ResultSet rs) throws SQLException {
    return new AuthorRequest(
        rs.getInt("request_id"),
        rs.getInt("user_id"),
        RequestStatus.valueOf(rs.getString("status")),
        rs.getTimestamp("requested_at").toLocalDateTime(),
        rs.getTimestamp("approved_at") != null ? rs.getTimestamp("approved_at").toLocalDateTime() : null,
        rs.getTimestamp("rejected_at") != null ? rs.getTimestamp("rejected_at").toLocalDateTime() : null
    );
  }

  public List<AuthorRequest> findAll() throws SQLException {
    String sql = "SELECT * FROM author_request ORDER BY requested_at DESC";
    List<AuthorRequest> requests = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        requests.add(mapRow(rs));
      }
    }

    return requests;
  }

}
