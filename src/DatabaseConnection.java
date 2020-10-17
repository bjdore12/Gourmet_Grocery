/*

    Author:     Benjamin J. Dore
    Date:       10/17/2020

    Description:
        This class defines the connection to the SQLite database, should be used on every DB class to
        ensure connection.

*/

import java.sql.*;

public class DatabaseConnection {
    private static Connection con;

    // This static block will initiate the connection to the database on startup and assign it to 'con'.
    static {
        try {
            con = connect();
        } catch (SQLException throwables) {
            throwables.printStackTrace();       // If an issue occurs (i.e. DB driver is missing), throw an exception.
        }
    }

    // Connect to the SQLite database.
    private static Connection connect() throws  SQLException {
        // Database path -- if it's new database, it will be created in the project folder
        return DriverManager.getConnection("jdbc:sqlite:SQLiteGG.db");
    }

    // Return the connection to the caller.
    public static Connection getDBConnection() {
        return con;
    }
}
