package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/smartbank_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // empty string by default in XAMPP

    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connected successfully.");
            return conn;
        } catch (SQLException e) {
            System.out.println("❌ DB Connection failed: " + e.getMessage());
            return null;
        }
    }
}
