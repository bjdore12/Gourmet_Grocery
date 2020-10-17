/*

    Author:     Benjamin J. Dore
    Date:       10/17/2020

    Description:
        This class defines the Inventory_To_Order_Table and is responsible for table startup and information gathering/setting.
        This table is important for associating an inventory item to an order and includes data like quantity requested on an order
        and costs.

*/

import java.sql.*;

public class InventoryToOrderDB {
    private static Connection con;

    // Call the database connection and get connected to the DB.
    static {
        con = DatabaseConnection.getDBConnection();
    }

    // Does the database already exist? Check the sqlite_master table for the Inventory_To_Order_Table
    public static boolean databaseExists() throws SQLException {
        Statement state;
        ResultSet res;

        // Check for database table existence and if it's not there, create it.
        state = con.createStatement();
        res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Inventory_To_Order_Table'");

        if (res.next())      // If the table doesn't exist, this will return false.
            return true;
        else
            return false;
    }

    // Build the Inventory_To_Order_Table database
    public static void buildDatabase() throws SQLException {
        Statement state;

        if (!databaseExists()) {

            state = con.createStatement();
            state.executeUpdate("CREATE TABLE Inventory_To_Order_Table(" +
                    "ORDER_TUID INTEGER," +
                    "Inventory_TUID INTEGER," +
                    "Quantity INTEGER," +
                    "Inventory_Unit_Price DOUBLE);");
        }
    }

    // Function will associate and Inventory item to an Order and assign quantity.
    public static boolean addOrderToInventory(int Order_TUID, int Inventory_TUID, int Quantity) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        ResultSet itemDetails = InventoryDB.getInventoryItemDetails(Inventory_TUID);        // get the inventory item details we need.
        Quantity = getOrderQuantityFromInventory(Order_TUID, Inventory_TUID, Quantity);     // get the quantity for the order
        InventoryDB.changeQuantity(Inventory_TUID, -Quantity);                              // Change the quantity in the Inventory table

        PreparedStatement prep;

        prep = con.prepareStatement("INSERT INTO Inventory_To_Order_Table VALUES(?,?,?,?);");
        prep.setInt(1, Order_TUID);
        prep.setInt(2, Inventory_TUID);
        prep.setInt(3, Quantity);
        prep.setDouble(4, itemDetails.getDouble("Unit_Price") * Quantity);

        return prep.execute();      // Add the association to the table.
    }

    // Will modify an existing association between an inventory item and an order
    public static boolean modifyOrderToInventory(int Order_TUID, int Inventory_TUID, int quantityChange) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        boolean associationExists = inventoryToOrderExists(Order_TUID, Inventory_TUID); // First check and see if this association already exists.

        PreparedStatement prep;

        // If the association exists...
        if (associationExists) {
            InventoryDB.changeQuantity(Inventory_TUID, -quantityChange);        // Change the quantity in the inventory.

            ResultSet invToOrderDetails = getInventoryToOrderDetails(Order_TUID, Inventory_TUID);   // Get the details for the order association.

            int newQuantity = invToOrderDetails.getInt("Quantity") + quantityChange;    // Get the updated quantity for the order association.

            prep = con.prepareStatement("UPDATE Inventory_To_Order_Table\n" +
                    "SET Quantity =  ?,\n" +
                    "    Inventory_Unit_Price = (?) * (SELECT Unit_Price FROM Inventory_Table WHERE TUID = ?)\n" +
                    "WHERE Order_TUID = ? AND Inventory_TUID = ?;");

            prep.setInt(1, newQuantity);
            prep.setInt(2, newQuantity);
            prep.setInt(3, Inventory_TUID);
            prep.setInt(4, Order_TUID);
            prep.setInt(5, Inventory_TUID);

            return prep.execute();      // Set the modifications and return true if succuessful.
        } else {
            // Add the association if it doesn't exist. This basically happens when you try to modify an order and add items that were
            // not there before.
            return addOrderToInventory(Order_TUID, Inventory_TUID, quantityChange); // This will return true if successful, false otherwise.
        }
    }

    // This will wipe out the order to inventory associations to empty. To be run on startup and in conjunction with Order_Table reset.
    public static void resetInventoryToOrder() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Inventory_To_Order_Table");
        prep.execute();
    }

    // Function will return the price of an order association based on the quantity.
    public static ResultSet getInventoryToOrderPrice(int Order_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT SUM(Inventory_Unit_Price) AS total_cost FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID);
        return res;
    }

    // This function will pull items from the inventory and return them to the caller. Use this to assign inventory items to an order association.
    public static int getOrderQuantityFromInventory(int Order_TUID, int Inventory_TUID, int requestedQuantity) throws SQLException {
        ResultSet itemDetails = InventoryDB.getInventoryItemDetails(Inventory_TUID);    // Get inventory item details.
        int quantity = itemDetails.getInt("Quantity");      // Get the quantity in inventory

        if (quantity >= requestedQuantity)
            return requestedQuantity;       // Return the quantity requested if there is enough in stock.
        else {
            // There is not enough inventory for the requested item! Notify the user and allocate whatever is left for the requested item.
            System.out.println("(ORDER #" + Reporter.formatTUID(Order_TUID) + ") ALERT: requested quantity is too high for item " + Reporter.formatTUID(Inventory_TUID) + " (" + itemDetails.getString("Item_Name")+ ") \n\t\trequested: " + requestedQuantity + "\n\t\tavailable: " + quantity);

            if (quantity != 0) System.out.println("Adding " + quantity + " items to the order #" + Reporter.formatTUID(Order_TUID) + "\n");
            else System.out.println("item not added to order #" + Reporter.formatTUID(Order_TUID) + "\n");
            return quantity;    // The quantity will be at least 0, it is not possible to get a quantity that is negative.
        }
    }

    // Check to see if an inventory item is associated with an order.
    public static boolean inventoryToOrderExists(int Order_TUID, int Inventory_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID + " AND Inventory_TUID = " + Inventory_TUID);

        if (res.next())
            return true;
        else
            return false;
    }

    // Get the details for an association based on Order ID and Inventory item ID.
    public static ResultSet getInventoryToOrderDetails(int Order_TUID, int Inventory_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID + " AND Inventory_TUID = " + Inventory_TUID);

        return res;
    }

    // Get the details for an association for a Order ID, useful if you want to get all the items for an order.
    public static ResultSet getInventoryToOrderDetails(int Order_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID);

        return res;
    }

    // Get the total quantity of items for an order.
    public static int getOrderTotalQuantity(int Order_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT SUM(Quantity) AS total_quantity FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID);

        return res.getInt("total_quantity");
    }

    // Get the total quantity of items based on Order ID and Inventory item ID.
    public static int getOrderTotalQuantity(int Order_TUID, int Inventory_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT SUM(Quantity) AS total_quantity FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID + " AND Inventory_TUID = " + Inventory_TUID);

        return res.getInt("total_quantity");
    }

    // This function is to be run when the quantity for an order assocation reaches zero, we want that item removed from the
    // order so this will basically cancel an item.
    public static boolean deleteInvToOrderAssocationIfZeroQuantity(int Order_TUID) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        PreparedStatement prep;
        int orderQuantity = getOrderTotalQuantity(Order_TUID);  // Get the quantity for an order association

        deleteAssociation(Order_TUID);      // Delete all inventory assications, function will only delete those with quantity = 0

        // If the entire order has no quantity....
        if (orderQuantity == 0) {
            OrderDB.deleteOrder(Order_TUID);    // Then delete the whole order.
            prep = con.prepareStatement("DELETE FROM Inventory_To_Order_Table WHERE ORDER_TUID = ?");   // and delete all associations.
            prep.setInt(1, Order_TUID);
            return prep.execute();
        }

        return false;
    }

    // Delete an assocation between an inventory item and order
    public static boolean deleteAssociation(int Order_TUID) throws SQLException {
        if (!databaseExists())  // Check to see if the table exists first.
            buildDatabase();

        PreparedStatement prep;

        ResultSet orderDetails = getInventoryToOrderDetails(Order_TUID);    // Get details on item/order association.

        while (orderDetails.next()) {
            // If an assocation has a quantity = 0, then delete that assocation (the item has been cancelled).
            if (orderDetails.getInt("Quantity") == 0) {
                prep = con.prepareStatement("DELETE FROM Inventory_To_Order_Table WHERE ORDER_TUID = ? AND Inventory_TUID = ?");
                prep.setInt(1, Order_TUID);
                prep.setInt(2, orderDetails.getInt("Inventory_TUID"));

                return prep.execute();
            }
        }
        return false;
    }
}
