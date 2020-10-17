/*

    Author:     Benjamin J. Dore
    Date:       10/17/2020

    Description:
        This class defines the Customer_Table database, responsibilities include setting up the DB on initial startup
        (i.e. when no DB and table is created), as well as getting customer information and resetting the customer DB.

*/

import java.sql.*;

public class CustomerDB {

    private static Connection con;


    // Call the database connection and get connected to the DB.
    static {
        con = DatabaseConnection.getDBConnection();
    }

    // Does the database already exist? Check the sqlite_master table for the Customer_Table
    public static boolean databaseExists() throws SQLException {
        Statement state;
        ResultSet res;

        // Check for database table existence and if it's not there, create it.
        state = con.createStatement();
        res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Customer_Table'");

        if (res.next())     // If the table doesn't exist, this will return false.
            return true;
        else
            return false;
    }

    // Build the Customer_Table database
    public static void buildDatabase() throws SQLException {
        Statement state;

        // Check to see if the database already exists, only create the table if it isn't already there.
        if (!databaseExists()) {

            state = con.createStatement();
            state.executeUpdate("CREATE TABLE Customer_Table(" +
                    "TUID INTEGER," +
                    "First_Name VARCHAR(60)," +
                    "Last_Name VARCHAR(60)," +
                    "Phone VARCHAR(60)," +
                    "PRIMARY KEY (TUID));");
        }
    }

    // Add a customer record into the Customer_Table
    public static boolean addCustomer(int TUID, String firstName, String lastName, String phone) throws SQLException {
        if (!databaseExists())  // This check ensures that we don't add a customer to a table that doesn't exist.
            buildDatabase();

        PreparedStatement prep;

        prep = con.prepareStatement("INSERT INTO Customer_Table VALUES(?,?,?,?);");
        prep.setInt(1, TUID);
        prep.setString(2, firstName);
        prep.setString(3, lastName);
        prep.setString(4, phone);

        return prep.execute();  // Will return true if the operation is successful.
    }

    // Return all the customers from the Customer_Table database.
    public static ResultSet getCustomers() throws SQLException {
        if (!databaseExists()) // This check ensures that we don't try to get a customer from a table that doesn't exist.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Customer_Table");
        return res;     // Return a ResultSet containing the customers in the Customer_Table
    }

    // Get a specific customer from the Customer_Table using their TUID
    public static ResultSet getCustomers(int TUID) throws SQLException {
        if (!databaseExists()) // This check ensures that we don't try to get a customer from a table that doesn't exist.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Customer_Table WHERE TUID = " + TUID);
        return res;
    }

    // Check to see if a customer already exists in the Customer_Table
    public static boolean customerExists(int TUID) throws SQLException {
        if (!databaseExists())  // This check ensures that we don't try to verify a customers existence from a table that doesn't exist.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Customer_Table WHERE TUID = " + TUID);
        if (res.next())
            return true;    // Customer exists
        else
            return false;   // Customer does not exist
    }

    // Reset the Customer_Table to be empty, this function is to be run on startup when the user wants to reset to default.
    public static void resetCustomers() throws SQLException {
        if (!databaseExists())  // This check ensures that we don't try to reset the Customer_Table if it doesn't exist.
            buildDatabase();

        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Customer_Table");
        prep.execute();
    }
}
