import java.sql.*;

public class DatabaseConnection {
    private static Connection con;

    static {
        try {
            con = getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static Connection getConnection() throws  SQLException {
        // Database path -- if it's new database, it will be created in the project folder
        return DriverManager.getConnection("jdbc:sqlite:SQLiteGG.db");
    }

    public static Connection getDBConnection() {
        return con;
    }
}
