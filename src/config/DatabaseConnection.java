package config;

import java.sql.Connection;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/mydatabase";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    private static Connection connection;

    

}
