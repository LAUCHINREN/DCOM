package hrm.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection to PostgreSQL.
 * Lives on the SERVER laptop — client never touches this class.
 *
 * Change DB_HOST to your DB laptop's IP when running across machines.
 * For local testing on one machine, keep it as localhost.
 */

public class DBConnection {

    // ---------------------------------------------------------------
    // Change these when deploying across 3 laptops:
    //   DB_HOST  = IP address of the DB laptop   (e.g. "192.168.1.105")
    //   DB_PORT  = PostgreSQL port               (default 5432)
    //   DB_NAME  = your database name
    //   USER     = your postgres username
    //   PASSWORD = your postgres password
    // ---------------------------------------------------------------

    private static final String DB_HOST  = "localhost";
    private static final String DB_PORT  = "5432";
    private static final String DB_NAME  = "postgres";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "DCOM123";

    private static final String URL =
            "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    private static Connection connection;

    // Private constructor — no one should instantiate this
    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("[DB] Opening new connection to " + URL);
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            // Set a timeout for the connection
            connection.setNetworkTimeout(java.util.concurrent.Executors.newSingleThreadExecutor(), 10000);
            if (connection != null && !connection.isClosed() && connection.isValid(5)) {
                System.out.println("Connection is successful and active!");
            }
            //System.out.println("[DB] Connection established.");
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}
