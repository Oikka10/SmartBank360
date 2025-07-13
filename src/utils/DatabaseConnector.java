package utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnector {

    private static final String URL = "jdbc:mysql://localhost:3306/smartbank_db"; // ✅ fixed name
    private static final String USER = "root";
    private static final String PASSWORD = ""; // or your MySQL password

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
