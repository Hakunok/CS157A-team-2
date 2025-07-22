package com.airchive.db;

import com.airchive.util.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnectionManager {

  private static final String JDBC_URL =
      "jdbc:mysql://localhost:3306/" + Config.getProperty("db.dbName")
          + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
  private static final String DB_USER = Config.getProperty("db.user");
  private static final String DB_PASSWORD = Config.getProperty("db.password");

  static {
    try {
      Class.forName(Config.getProperty("db.driver"));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Failed to load JDBC driver.", e);
    }
  }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
  }
}
