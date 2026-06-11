package esportsapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:oracle:thin:@coeoracle.aus.edu:1521:orcl";
    private static final String USER = "b00098368";
    private static final String PASS = "b00098368";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}