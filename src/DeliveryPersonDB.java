/*

    Author:     Benjamin J. Dore
    Date:       10/17/2020

    Description:
        This class defines the DeliveryPerson_Table, and is responsible for table creation and the operations
        to return Delivery Person information. For now, the table only is provisioned to include 'Barb' and 'Alan'
        plus their applicable information.

*/

import java.sql.*;

public class DeliveryPersonDB {
    private static Connection con;

    // Call the database connection and get connected to the DB.
    static {
        con = DatabaseConnection.getDBConnection();
    }

    // Does the database already exist? Check the sqlite_master table for the DeliveryPerson_Table
    public static boolean databaseExists() throws SQLException {
        Statement state;
        ResultSet res;

        // Check for database table existence and if it's not there, create it.
        state = con.createStatement();
        res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='DeliveryPerson_Table'");

        if (res.next())      // If the table doesn't exist, this will return false.
            return true;
        else
            return false;
    }

    // Build the DeliveryPerson_Table database
    public static void buildDatabase() throws SQLException {
        Statement state;

        // Check to see if the database already exists, only create the table if it isn't already there.
        if (!databaseExists()) {

            state = con.createStatement();
            state.executeUpdate("CREATE TABLE DeliveryPerson_Table(" +
                    "TUID INTEGER," +
                    "Name VARCHAR(60)," +
                    "Pay_Rate DOUBLE," +
                    "PRIMARY KEY (TUID));");

            populateDeliveryPersons();      // Populate the table with Barb and Alan's information
        }
    }

    // This function will run when the DeliveryPerson_Table is created, will populate the table
    // with data for Alan and Barb.
    private static void populateDeliveryPersons() throws SQLException {

        PreparedStatement prep;

        prep = con.prepareStatement("INSERT INTO DeliveryPerson_Table VALUES(?,?,?);");
        prep.setInt(1, 101);
        prep.setString(2, "Barb");
        prep.setDouble(3, 22.50);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO DeliveryPerson_Table VALUES(?,?,?);");
        prep.setInt(1, 102);
        prep.setString(2, "Alan");
        prep.setDouble(3, 19.00);
        prep.execute();
    }

    // Will return a ResultSet of all the Delivery Person information
    public static ResultSet getDeliveryPersons() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT *  FROM DeliveryPerson_Table");
        return res;
    }

    // Will return a ResultSet of a specific Delivery Person's information.
    public static ResultSet getDeliveryPersons(int TUID) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM DeliveryPerson_Table WHERE TUID = " + TUID);
        return res;
    }
}
