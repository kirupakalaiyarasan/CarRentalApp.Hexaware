package com.carrental.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionUtil {

    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                String connectionString = PropertyUtil.getPropertyString("db.properties");
                if (connectionString != null) {
                    connection = DriverManager.getConnection(connectionString);
                    System.out.println("Connection established successfully.");
                } else {
                    System.out.println("Failed to retrieve connection string.");
                }
            } catch (SQLException e) {
                System.out.println("Error establishing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }
}
