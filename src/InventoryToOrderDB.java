import java.sql.*;

public class InventoryToOrderDB {
    private static Connection con;

    static {
        con = DatabaseConnection.getDBConnection();
    }

    public static boolean databaseExists() throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Inventory_To_Order_Table'");

        if (res.next())
            return true;
        else
            return false;
    }

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


    public static boolean addOrderToInventory(int Order_TUID, int Inventory_TUID, int Quantity) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        ResultSet itemDetails = InventoryDB.getInventoryItemDetails(Inventory_TUID);
        Quantity = getOrderQuantityFromInventory(Inventory_TUID, Quantity);
        InventoryDB.changeQuantity(Inventory_TUID, -Quantity);

        PreparedStatement prep;

        prep = con.prepareStatement("INSERT INTO Inventory_To_Order_Table VALUES(?,?,?,?);");
        prep.setInt(1, Order_TUID);
        prep.setInt(2, Inventory_TUID);
        prep.setInt(3, Quantity);
        prep.setDouble(4, itemDetails.getDouble("Unit_Price") * Quantity);

        return prep.execute();
    }

    public static boolean modifyOrderToInventory(int Order_TUID, int Inventory_TUID, int quantityChange) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        boolean associationExists = inventoryToOrderExists(Order_TUID, Inventory_TUID);

        PreparedStatement prep;

        if (associationExists) {
            InventoryDB.changeQuantity(Inventory_TUID, -quantityChange);

            ResultSet invToOrderDetails = getInventoryToOrderDetails(Order_TUID, Inventory_TUID);

            int newQuantity = invToOrderDetails.getInt("Quantity") + quantityChange;

            prep = con.prepareStatement("UPDATE Inventory_To_Order_Table\n" +
                    "SET Quantity =  ?,\n" +
                    "    Inventory_Unit_Price = (?) * (SELECT Unit_Price FROM Inventory_Table WHERE TUID = ?)\n" +
                    "WHERE Order_TUID = ? AND Inventory_TUID = ?;");

            prep.setInt(1, newQuantity);
            prep.setInt(2, newQuantity);
            prep.setInt(3, Inventory_TUID);
            prep.setInt(4, Order_TUID);
            prep.setInt(5, Inventory_TUID);

            return prep.execute();
        } else {
            return addOrderToInventory(Order_TUID, Inventory_TUID, quantityChange);
        }
    }

    public static void resetInventoryToOrder() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Inventory_To_Order_Table");
        prep.execute();
    }

    public static ResultSet getInventoryToOrderPrice(int Order_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT SUM(Inventory_Unit_Price) AS total_cost FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID);
        return res;
    }

    public static int getOrderQuantityFromInventory(int Inventory_TUID, int requestedQuantity) throws SQLException {
        ResultSet itemDetails = InventoryDB.getInventoryItemDetails(Inventory_TUID);
        int quantity = itemDetails.getInt("Quantity");

        if (quantity >= requestedQuantity)
            return requestedQuantity;
        else {
            System.out.println("ERROR: requested quantity is too high for item " + Inventory_TUID + " \n\t\trequested: " + requestedQuantity + "\n\t\tavailable: " + quantity);
            System.out.println("Adding " + quantity + " items to the order\n");
            return quantity;
        }
    }

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

    public static ResultSet getInventoryToOrderDetails(int Order_TUID, int Inventory_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID + " AND Inventory_TUID = " + Inventory_TUID);

        return res;
    }

    public static ResultSet getInventoryToOrderDetails(int Order_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID);

        return res;
    }

    public static int getOrderTotalQuantity(int Order_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT SUM(Quantity) AS total_quantity FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID);

        return res.getInt("total_quantity");
    }

    public static int getOrderTotalQuantity(int Order_TUID, int Inventory_TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT SUM(Quantity) AS total_quantity FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID + " AND Inventory_TUID = " + Inventory_TUID);

        return res.getInt("total_quantity");
    }

    public static boolean deleteInvToOrderAssocationIfZeroQuantity(int Order_TUID) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        PreparedStatement prep;
        int orderQuantity = getOrderTotalQuantity(Order_TUID);

        deleteAssociation(Order_TUID);

        if (orderQuantity == 0) {
            OrderDB.deleteOrder(Order_TUID);
            prep = con.prepareStatement("DELETE FROM Inventory_To_Order_Table WHERE ORDER_TUID = ?");
            prep.setInt(1, Order_TUID);
            return prep.execute();
        }

        return false;
    }

    public static boolean deleteAssociation(int Order_TUID) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        PreparedStatement prep;

        ResultSet orderDetails = getInventoryToOrderDetails(Order_TUID);

        while (orderDetails.next()) {
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
