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

/**
 * Abstract base class for all repository implementations in this application.
 *
 * <p>This class encapsulates common JDBC operations and exception handling patterns, reducing
 * boilerplate and maintaining consistency across repositories.</p>
 */
public abstract class BaseRepository {

  /**
   * Executes the provided function with a database connection and manages the connection
   * lifecycle. This method allows for a repository to implement a transactional only
   * method.
   *
   * @param <T> the type of the result produced by the provided function
   * @param function a function that operates on a {@link Connection} and returns a result
   * @return the result of the executed function
   * @throws DataAccessException if a database access error occurs
   */
  protected <T> T withConnection(SqlFunction<Connection, T> function) {
    try (Connection conn = DbConnectionManager.getConnection()) {
      return function.apply(conn);
    } catch (SQLException e) {
      throw new DataAccessException("Database error", e);
    }
  }

  /**
   * Executes a SQL query expected to return a single result.
   *
   * @param <T> the type of the object to map and return
   * @param conn the database connection to use
   * @param sql the SQL query string to execute
   * @param mapper a {@code RowMapper} to map the result set to an object of type T
   * @param params the parameters to be set on the {@link PreparedStatement}
   * @return an {@link Optional} containing the mapped object if a record is found; otherwise an
   * empty {@link Optional}
   * @throws DataAccessException if a database access error occurs while executing the query
   */
  protected <T> Optional<T> findOne(Connection conn, String sql, RowMapper<T> mapper, Object... params) {
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameters(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? Optional.of(mapper.mapRow(rs)) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error executing findOne", e);
    }
  }

  /**
   * Executes a query that may return multiple results.
   *
   * @param <T> the type of the object to map and return
   * @param conn the database connection to use
   * @param sql the SQL query string to execute
   * @param mapper a {@code RowMapper} to map the result set to an object of type T
   * @param params the parameters to be set on the {@link PreparedStatement}
   * @return a list of mapped objects; empty if no rows are returned
   * empty {@link List}
   * @throws DataAccessException if a database access error occurs while executing the query
   */
  protected <T> List<T> findMany(Connection conn, String sql, RowMapper<T> mapper, Object... params) {
    List<T> result = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameters(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          result.add(mapper.mapRow(rs));
        }
      }
      return result;
    } catch (SQLException e) {
      throw new DataAccessException("Error executing findMany", e);
    }
  }

  /**
   * Executes a query that returns a single column from multiple rows.
   *
   * @param <T> the type of the object to map and return
   * @param conn the database connection to use
   * @param sql the SQL query string to execute
   * @param columnType the class type of the column to retrieve
   * @param params the parameters to be set on the {@link PreparedStatement}
   * @return a list of mapped objects; empty if no rows are returned
   * @throws DataAccessException if a database access error occurs while executing the query
   */
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
      throw new DataAccessException("Error fetching column values", e);
    }
  }

  /**
   * Checks whether a record exists in the database based on the provided query and parameters.
   *
   * @param conn the database connection to use
   * @param sql the SQL SELECT EXISTS(...) query to run
   * @param params the parameters to be set on the {@link PreparedStatement}
   * @return true if a matching record exists; false otherwise
   * @throws DataAccessException if a database access error occurs while executing the query
   */
  protected boolean exists(Connection conn, String sql, Object... params) {
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameters(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() && rs.getBoolean(1);
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error checking existence", e);
    }
  }

  /**
   * Executes an INSERT SQL operation using the provided database connection, SQL query, and
   * parameters.
   *
   * @param conn the database connection to use for executing the statement
   * @param sql the SQL INSERT query to execute
   * @param params the parameters to be set on the {@link PreparedStatement}, corresponding to placeholders in the SQL query
   * @return the generated key of the inserted row
   * @throws DataAccessException if an error occurs while executing the statement or retrieving the generated key
   */
  protected int executeInsertWithGeneratedKey(Connection conn, String sql, Object... params) {
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      setParameters(stmt, params);
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        if (keys.next()) return keys.getInt(1);
        throw new DataAccessException("Insert succeeded but no ID returned.");
      }
    } catch (SQLException e) {
      throw new DataAccessException("Insert failed", e);
    }
  }

  /**
   * Executes an UPDATE or DELETE SQL operation using the provided database connection, SQL query,
   * and parameters.
   *
   * @param conn the database connection to use for executing the update or delete operation
   * @param sql the SQL query string to execute for the update or delete
   * @param params the parameters to bind to the placeholders in the SQL query
   * @return the number of rows affected by the executed update or delete operation
   * @throws DataAccessException if an error occurs while preparing, setting parameters,
   * or executing the statement
   */
  protected int executeUpdate(Connection conn, String sql, Object... params) {
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameters(stmt, params);
      return stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException("Update/delete failed", e);
    }
  }

  /**
   * Sets positional parameters on a {@link PreparedStatement}.
   *
   * @param stmt the PreparedStatement to set
   * @param params the variable length parameters to set
   * @throws DataAccessException if an SQLException occurs when setting the parameters
   */
  private void setParameters(PreparedStatement stmt, Object... params) {
    try {
      for (int i = 0; i < params.length; i++) {
        stmt.setObject(i + 1, params[i]);
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error setting parameters", e);
    }
  }

  /**
   * Functional interface for mapping a single row of a {@link ResultSet} to an object of type
   * {@code T}.
   */
  @FunctionalInterface
  public interface RowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
  }

  /**
   * A functional interface representing a function that can throw a {@link SQLException}.
   * Used by the {@link #withConnection(SqlFunction)} method to execute code requiring a
   * {@link Connection} object.
   */
  @FunctionalInterface
  public interface SqlFunction<T, R> {
    R apply(T t) throws SQLException;
  }
}