package com.carrental.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyUtil {

    public static String getPropertyString(String propertyFileName) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(propertyFileName)) {
            props.load(fis);

            // Fetch properties
            String host = props.getProperty("host");
            String port = props.getProperty("port");
            String dbname = props.getProperty("dbname");
            String username = props.getProperty("username");
            String password = props.getProperty("password");

            // Validate required properties
            if (host == null || port == null || dbname == null || username == null || password == null) {
                throw new IllegalArgumentException("Missing database configuration properties in " + propertyFileName);
            }

            // Create connection string
            return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s", host, port, dbname, username, password);

        } catch (IOException e) {
            System.out.println("Error reading property file: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("Configuration error: " + e.getMessage());
        }
        return null;
    }
}
