package com.airchive.repository;

import com.airchive.db.DbConnectionManager;
import com.airchive.exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseRepository {

  protected <T> Optional<T> findOne(Connection conn, String sql, RowMapper<T> mapper, Object... params) {
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameters(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapper.mapRow(rs));
        }
      }
      return Optional.empty();
    } catch (SQLException e) {
      throw new DataAccessException("A database error occurred.", e);
    }
  }

  protected <T> List<T> findMany(Connection conn, String sql, RowMapper<T> mapper, Object... params) {
    List<T> results = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameters(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(mapper.mapRow(rs));
        }
      }
      return results;
    } catch (SQLException e) {
      throw new DataAccessException("A database error occurred.", e);
    }
  }

  protected <T> List<T> findColumnMany(Connection conn, String sql, Class<T> columnType, Object... params) {
    List<T> results = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameters(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(rs.getObject(1, columnType));
        }
      }
      return results;
    } catch (SQLException e) {
      throw new DataAccessException("A database error occurred while fetching column values.", e);
    }
  }


  protected int executeUpdate(Connection conn, String sql, Object... params) {
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameters(stmt, params);
      return stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException("A database error occurred.", e);
    }
  }

  protected int executeInsertWithGeneratedKey(Connection conn, String sql, Object... params) {
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      setParameters(stmt, params);
      stmt.executeUpdate();
      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          return generatedKeys.getInt(1);
        } else {
          throw new DataAccessException("A database error occurred.");
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("A database error occurred.", e);
    }
  }

  protected boolean exists(Connection conn, String sql, Object... params) {
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameters(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() && rs.getBoolean(1);
      }
    } catch (SQLException e) {
      throw new DataAccessException("A database error occurred.", e);
    }
  }

  protected <T> T executeWithConnection(ConnectionCallback<T> callback) {
    try (Connection conn = DbConnectionManager.getConnection()) {
      return callback.execute(conn);
    } catch (Exception e) {
      if (e instanceof DataAccessException) throw (DataAccessException) e;
      if (e instanceof RuntimeException) throw (RuntimeException) e;
      throw new DataAccessException("A database error occurred.", e);
    }
  }

  private void setParameters(PreparedStatement stmt, Object... params) {
    try {
      for (int i = 0; i < params.length; i++) {
        stmt.setObject(i + 1, params[i]);
      }
    } catch (SQLException e) {
      throw new DataAccessException("A database error occurred.", e);
    }
  }

  @FunctionalInterface
  public interface RowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
  }

  @FunctionalInterface
  public interface ConnectionCallback<T> {
    T execute(Connection conn);
  }
}