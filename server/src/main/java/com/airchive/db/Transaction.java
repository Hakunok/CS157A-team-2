package com.airchive.db;

import com.airchive.exception.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class provides an abstraction for safely managing ACID JDBC transactions using a {@link Connection}
 * obtained from {@link DbConnectionManager}.
 * <p>
 * This class supports atomicity by disabling auto-commit and allowing control over commit and rollback.
 * It implements {@link AutoCloseable} so that it can be used within a try-with-resources block. If {@link #commit()}
 * is not called before closing the transaction, the transaction is automatically rolled back to ensure consistency.
 * <p>
 * Regardless of success/failure the {@link Connection} is closed and auto-commit is set back to {@code true}.
 * <p>
 * Usage:
 * <pre>{@code
 * try (Transaction tx = new Transaction()) {
 *   tx.begin();
 *   Connection conn = tx.getConnection();
 *   // perform database operations
 *   tx.commit(); // must be called
 * }
 * }</pre>
 */
public class Transaction implements AutoCloseable {
  private Connection connection;
  private boolean committed = false;

  /**
   * Creates a new transaction by obtaining a {@code Connection} from {@link DbConnectionManager}.
   *
   * @throws DataAccessException if the connection could not be obtained
   */
  public Transaction() throws DataAccessException {
    try {
      this.connection = DbConnectionManager.getConnection();
    } catch (SQLException e) {
      throw new DataAccessException("Failed to get a database connection.", e);
    }
  }

  /**
   * Returns the {@link Connection} associated with the transaction.
   * <p>
   * Only the connection returned from this method should be used in a transactional context.
   *
   * @return a JDBC connection tied to this transaction
   */
  public Connection getConnection() {
    return this.connection;
  }

  /**
   * Begins the transaction by disabling auto-commit on the connection.
   *
   * @throws DataAccessException if the operation fails
   */
  public void begin() throws DataAccessException {
    try {
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new DataAccessException("Failed to begin transaction.", e);
    }
  }

  /**
   * Commits the transaction.
   * <p>
   * If not called before closing, the transaction will be rolled back.
   *
   * @throws DataAccessException if the commit fails
   */
  public void commit() throws DataAccessException {
    try {
      connection.commit();
      this.committed = true;
    } catch (SQLException e) {
      throw new DataAccessException("Failed to commit transaction.", e);
    }
  }

  /**
   * Closes the transaction.
   * <p>
   * If {@link #commit()} was not called, the transaction is rolled back automatically.
   * This method always restores auto-commit and closes the connection.
   *
   * @throws DataAccessException if rollback or connection close fails
   */
  @Override
  public void close() throws DataAccessException {
    try {
      if (connection != null && !committed) {
        connection.rollback();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Failed to rollback transaction.", e);
    } finally {
      try {
        if (connection != null) {
          connection.setAutoCommit(true);
          connection.close();
        }
      } catch (SQLException e) {
        throw new DataAccessException("Failed to close connection.", e);
      }
    }
  }
}