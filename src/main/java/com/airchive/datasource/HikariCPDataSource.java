package com.airchive.datasource;

import com.airchive.util.PropertyLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * FIXME: Don't use yet, got to ask Professor if we are allowed to use connection pooling libraries.
 * DataSource implementation using HikariCP for connection pooling.
 */
public class HikariCPDataSource implements DataSource {
  private HikariDataSource ds;

  public HikariCPDataSource() {
    String username = PropertyLoader.getProperty("db.username");
    String password = PropertyLoader.getProperty("db.password");
    String database = PropertyLoader.getProperty("db.database");
    String url = "jdbc:mysql://localhost:3306/" + database + "?useSSL=false&serverTimezone=UTC";

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(url);
    config.setUsername(username);
    config.setPassword(password);
    config.setMaximumPoolSize(4);

    try {
      this.ds = new HikariDataSource(config);
      System.out.println("HikariCP DataSource created");
    } catch (Exception e) {
      System.out.println("HikariCP DataSource creation failed");
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return ds.getConnection();
  }

  // Irrelevant methods, just here to satisfy DataSource interface contract

  // HikariCP does not support this method
  // throws SQLFeatureNotSupportedException
  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return ds.getConnection(username, password);
  }

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
