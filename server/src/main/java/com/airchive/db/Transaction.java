package com.airchive.db;

import com.airchive.exception.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The {@code Transaction} class provides an abstraction for managing JDBC transactions using
 * {@link Connection}. It integrates with the {@link DbConnectionManager} and ensures
 * consistent transaction control.
 *
 * <p>Usage pattern with try-with-resources:</p>
 *
 * <pre>{@code
 * try (Transaction tx = new Transaction()) {
 *   tx.begin();
 *   Connection conn = tx.getConnection();
 *   // Perform DB operations using conn
 *   tx.commit(); // called explicitly
 * }
 * }</pre>
 *
 * <p>Note: if {@code commit()} is not called before the transaction is closed, the transaction
 * will be rolled back.</p>
 *
 * @see java.sql.Connection
 * @see DbConnectionManager
 */
public class Transaction implements AutoCloseable {
  private Connection connection;
  private boolean committed = false;

  /**
   * Creates a new transaction by obtaining a connection from {@link DbConnectionManager}.
   * @throws DataAccessException if the connection could not be established
   */
  public Transaction() throws DataAccessException {
    try {
      this.connection = DbConnectionManager.getConnection();
    } catch (SQLException e) {
      throw new DataAccessException("Failed to get a database connection.", e);
    }
  }

  public Connection getConnection() {
    return this.connection;
  }

  /**
   * Begins the transaction by setting {@code autoCommit} to {@code false}.
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
   * Commits the transaction. If not called, the transaction will be rolled back on close.
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
   * If {@link #commit()} was not called, the transaction is rolled back.
   * Always restores {@code autoCommit} and closes the underlying connection.
   * @throws DataAccessException if rollback or close fails
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
