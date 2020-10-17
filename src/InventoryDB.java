/*

    Author:     Benjamin J. Dore
    Date:       10/17/2020

    Description:
        This class defines the Inventory_Table and is responsible for table startup, getting and setting inventory items
        and resetting the inventory to default values on startup.

*/

import java.sql.*;
import java.text.NumberFormat;

public class InventoryDB {

    // Call the database connection and get connected to the DB.
    private static Connection con;
    static {
        con = DatabaseConnection.getDBConnection();
    }

    // Does the database already exist? Check the sqlite_master table for the Inventory_Table
    public static boolean databaseExists() throws SQLException {
        Statement state;
        ResultSet res;

        // Check for database table existence and if it's not there, create it.
        state = con.createStatement();
        res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Inventory_Table'");

        if (res.next())     // If the table doesn't exist, this will return false.
            return true;
        else
            return false;
    }

    // Build the Inventory_Table database
    public static void buildDatabase() throws SQLException {
        Statement state;

        if (!databaseExists()) {

            state = con.createStatement();
            state.executeUpdate("CREATE TABLE Inventory_Table(" +
                    "TUID INTEGER," +
                    "Item_Name VARCHAR(60)," +
                    "Quantity INTEGER," +
                    "Unit_Price DOUBLE," +
                    "PRIMARY KEY (TUID));");

            populateInventory();     // Populate the inventory with initial values.
        }
    }

    // This function will run on DB startup to populate the inventory with intial values.
    private static void populateInventory() throws SQLException {

        addInventoryItem(101, "Peanut Butter – Chunky", 100, 10.00);
        addInventoryItem(102, "Peanut Butter – Smooth", 100, 11.00);
        addInventoryItem(201, "Jelly – Strawberry", 100, 9.50);
        addInventoryItem(202, "Jelly – Raspberry", 100, 9.50);
        addInventoryItem(203, "Jelly – Peach", 100, 11.25);
        addInventoryItem(301, "Bread – White", 40, 17.00);
        addInventoryItem(302, "Bread – Wheat", 30, 19.00);
        addInventoryItem(303, "Bread – Sourdough", 40, 21.50);
        addInventoryItem(401, "Milk – White", 10, 17.00);
        addInventoryItem(402, "Milk – Chocolate", 15, 17.00);
        addInventoryItem(403, "Milk – White 2%", 20, 18.00);
        addInventoryItem(404, "Milk – Chocolate 2%", 20, 18.00);

    }

    // Get all of the inventory information and return them as a ResultSet
    public static ResultSet displayInventory() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, Item_Name, Quantity, Unit_Price FROM Inventory_Table");
        return res;
    }

    // Reset the Inventory_Table back to intial values, to be run on startup if the user wants to reset the system.
    public static void resetInventory() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Inventory_Table");
        prep.execute();
        populateInventory();
    }

    // Add an inventory item to the Inventory_Table, currently this function is used to prepopulate the inventory on DB
    // startup.
    public static boolean addInventoryItem(int TUID, String itemName, int quantity, double unitPrice) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        PreparedStatement prep;

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, TUID);
        prep.setString(2, itemName);
        prep.setInt(3, quantity);
        prep.setDouble(4, unitPrice);
        return prep.execute();
    }

    // Get the details for a specific inventory item by TUID, return as ResultSet
    public static ResultSet getInventoryItemDetails(int TUID) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, Item_Name, Quantity, Unit_Price FROM Inventory_Table WHERE TUID = " + TUID);
        return res;
    }

    // Modify the quantity of a specific inventory item.
    public static boolean changeQuantity(int TUID, int requestedQuantity) throws SQLException {
        ResultSet res = getInventoryItemDetails(TUID);  // Get the inventory item details for the specific TUID
        PreparedStatement prep;

        if (res.next())  {
            int quantity = res.getInt("Quantity");      // Get the current quantity.
            quantity = (quantity + requestedQuantity > 0) ? quantity + requestedQuantity : 0;   // Get the new quantity (Quantity cannot be less than 0)

            prep = con.prepareStatement("UPDATE Inventory_Table SET Quantity = ? WHERE TUID = ?");
            prep.setInt(1, quantity);
            prep.setInt(2, TUID);

            return prep.execute();      // Set the quantity and return true if successful.

        } else {
            return false;   // The inventory item doesn't exist.
        }
    }
}