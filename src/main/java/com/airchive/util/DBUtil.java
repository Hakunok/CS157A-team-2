package com.airchive.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
  private static String url;
  private static String user;
  private static String password;
  private static String driver;

  static {
    try (InputStream inputStream = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
      Properties props = new Properties();
      props.load(inputStream);

      url = props.getProperty("db.url");
      user = props.getProperty("db.user");
      password = props.getProperty("db.password");
      driver = props.getProperty("db.driver");

      Class.forName(driver);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, user, password);
  }
}
