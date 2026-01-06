package org.antonio.Entity;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
  private static final Dotenv dotenv = Dotenv.load();
  private static final String JDBC_URL = dotenv.get("JDBC_URL");
  private static final String USERNAME = dotenv.get("USERNAME");
  private static final String PASSWORD = dotenv.get("PASSWORD");

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
  }
}
