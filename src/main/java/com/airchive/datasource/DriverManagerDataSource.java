package com.airchive.datasource;

import com.airchive.util.PropertyLoader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * DataSource implementation using DriverManager.
 * Implemented DataSource, just in case Professor does not allow the use of HikariCP
 */
public class DriverManagerDataSource implements DataSource {
  private String jdbcUrl;
  private String username;
  private String password;
  private String driverClass;

  public DriverManagerDataSource() {
    this.driverClass = PropertyLoader.getProperty("db.driverClass");
    this.jdbcUrl = PropertyLoader.getProperty("db.url");
    this.username = PropertyLoader.getProperty("db.username");
    this.password = PropertyLoader.getProperty("db.password");

    try {
      Class.forName(driverClass);
      System.out.println("JDBC Driver loaded for DriverManagerDataSource");
    } catch (ClassNotFoundException e) {
      System.err.println("DB Driver class not found: " + driverClass);
      e.printStackTrace();
      throw new RuntimeException("Missing JDBC Driver class for DriverManagerDataSource", e);
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(jdbcUrl, username, password);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return DriverManager.getConnection(jdbcUrl, username, password);
  }

  // Irrelevant methods, just here to satisfy DataSource interface contract

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return null;
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {

  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {

  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return null;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }
}
