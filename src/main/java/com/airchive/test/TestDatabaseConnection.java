package com.airchive.test;

import com.airchive.datasource.DriverManagerDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabaseConnection {
  public static void main(String[] args) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    //HikariCPDataSource dataSource = new HikariCPDataSource();

    try (Connection conn = dataSource.getConnection()) {
      if (!conn.isClosed()) {
        System.out.println(":) connected to db");
      } else {
        System.out.println(":( conn is closed");
      }
    } catch (SQLException e) {
      System.err.println(":( db conn failed");
      e.printStackTrace();
    }
  }
}
