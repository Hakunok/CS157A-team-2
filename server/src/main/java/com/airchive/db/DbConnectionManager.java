package com.airchive.db;

import com.airchive.exception.DataAccessException;
import com.airchive.util.PropertyUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.servlet.ServletContextEvent;

/**
 * This class provides database connection management for our application.
 * <p>
 * This class supports both connection pooling (via {@link HikariDataSource}) and standard non-pooled
 * connections (via {@link DriverManager}), depending on the {@code db.usePool} property in {@code db.properties}.
 * <p>
 * At class load time, the JDBC driver is loaded, and the connection pool is conditionally initialized.
 * All configuration values such as JDBC URL, database credentials, and pooling options are resolved through {@link PropertyUtils}.
 * <p>
 * The method {@link #getConnection()} should be called to get a database connection. Connections should be
 * closed using try-with-resources. The {@link Transaction} class I impemented closes the Connection
 * as long as it is used within a try-with-resources.
 * <p>
 * The connection pool should be closed by calling {@link #closePool()} when the application is being terminated.
 */
public class DbConnectionManager {

  private static final String JDBC_URL =
      "jdbc:mysql://localhost:3306/" + PropertyUtils.getProperty("db.dbName")
          + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
  private static final String DB_USER = PropertyUtils.getProperty("db.user");
  private static final String DB_PASSWORD = PropertyUtils.getProperty("db.password");
  private static final boolean USE_POOL = PropertyUtils.getBooleanProperty("db.usePool", false);
  private static HikariDataSource dataSource;

  static {
    try {
      Class.forName(PropertyUtils.getProperty("db.driver"));

      // I used HikariCP's configuration guidelines to set these
      if (USE_POOL) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(JDBC_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(300000);
        dataSource = new HikariDataSource(config);
      }
    } catch (Exception e) {
      throw new DataAccessException("Failed to initialize database configuration", e);
    }
  }

  /**
   * Obtains a new {@link Connection} to the database.
   * <p>
   * If connection pooling is enabled, the connection is fetched from the Hikari connection pool.
   * Otherwise, a new connection is created and retrieved using {@link DriverManager}.
   * <p>
   * The caller of this method is responsible for closing the connection.
   *
   * @return a live {@code Connection} to the database
   * @throws SQLException if a database access error occurs
   */
  public static Connection getConnection() throws SQLException {
    if (USE_POOL) {
      return dataSource.getConnection();
    } else {
      return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }
  }

  /**
   * If pooling is enabled, this method shuts down the Hikari connection pool.
   * <p>
   * This method should be called when the application is shutting down, and is currently done by
   * {@link com.airchive.bootstrap.AppBootstrap#contextDestroyed(ServletContextEvent)}.
   */
  public static void closePool() {
    if (dataSource != null) {
      dataSource.close();
    }
  }
}