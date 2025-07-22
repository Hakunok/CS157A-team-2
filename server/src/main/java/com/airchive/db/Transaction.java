package com.airchive.db;

import com.airchive.exception.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;

public class Transaction implements AutoCloseable {
  private Connection connection;
  private boolean committed = false;

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

  public void begin() throws DataAccessException {
    try {
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new DataAccessException("Failed to begin transaction.", e);
    }
  }

  public void commit() throws DataAccessException {
    try {
      connection.commit();
      this.committed = true;
    } catch (SQLException e) {
      throw new DataAccessException("Failed to commit transaction.", e);
    }
  }

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
