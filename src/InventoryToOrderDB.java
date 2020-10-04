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

            System.out.println("Building the Order_Table");
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

        PreparedStatement prep;

        prep = con.prepareStatement("INSERT INTO Inventory_To_Order_Table VALUES(?,?,?,?);");
        prep.setInt(1, Order_TUID);
        prep.setInt(2, Inventory_TUID);
        prep.setInt(3, Quantity);
        prep.setDouble(4, itemDetails.getDouble("Unit_Price") * Quantity);

        return prep.execute();
    }

    public static boolean orderExists(int TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Order_Table WHERE TUID = " + TUID);
        if (res.next())
            return true;
        else
            return false;
    }

    public static void resetInventoryToOrder() throws SQLException {
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
}
