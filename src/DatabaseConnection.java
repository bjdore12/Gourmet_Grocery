import java.sql.*;

public class DatabaseConnection {
    private static Connection con;

    static {
        try {
            con = connect();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static Connection connect() throws  SQLException {
        // Database path -- if it's new database, it will be created in the project folder
        return DriverManager.getConnection("jdbc:sqlite:SQLiteGG.db");
    }

    public static Connection getDBConnection() {
        return con;
    }
}
