package com.airchive.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for safely closing JDBC resources.
 * Its sole purpose is to provide static helper methods for closing Connections, Statements, and ResultSets.
 */
public class DBConnectionUtil {

  // Private constructor to prevent instantiation, as it's a utility class
  private DBConnectionUtil() {
    throw new UnsupportedOperationException("Should not instantiate a DBConnectionUtil");
  }

  public static void close(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        System.err.println("Failed to close connection: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public static void close(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        System.err.println("Failed to close statement: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public static void close(ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (SQLException e) {
        System.err.println("Failed to close resultSet: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public static void close(ResultSet resultSet, Statement statement, Connection connection) {
    close(resultSet);
    close(statement);
    close(connection);
  }

  public static void close(ResultSet resultSet, PreparedStatement preparedStatement,
      Connection connection) {
    close(resultSet);
    close(preparedStatement);
    close(connection);
  }
}