import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    public static Connection connect() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                ConfigManager.getDatabaseUrl(),
                ConfigManager.getDatabaseUsername(),
                ConfigManager.getDatabasePassword()
            );
            System.out.println("Connected to the database successfully.");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
        return connection;
    }

    public static void main(String[] args) {
        Connection connection = null;
        try {
            connection = DatabaseConnector.connect();
            if (connection != null) {
                System.out.println("Connection test successful.");
            } else {
                System.out.println("Connection test failed.");
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Database connection closed after test.");
                } catch (SQLException e) {
                    System.out.println("Error closing the connection: " + e.getMessage());
                }
            }
        }
    }
}
