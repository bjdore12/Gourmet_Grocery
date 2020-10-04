import java.sql.*;

public class InventoryDB {

    private static Connection con;

    static {
        con = DatabaseConnection.getDBConnection();
    }

    public static boolean databaseExists() throws SQLException {
        Statement state;
        ResultSet res;

        // Check for database table existence and if it's not there, create it and add 2 records
        state = con.createStatement();
        res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Inventory_Table'");

        if (res.next())
            return true;
        else
            return false;
    }

    public static void buildDatabase() throws SQLException {
        Statement state;

        if (!databaseExists()) {

            System.out.println("Building the Inventory_Table table");
            state = con.createStatement();
            state.executeUpdate("CREATE TABLE Inventory_Table(" +
                    "TUID INTEGER," +
                    "Item_Name VARCHAR(60)," +
                    "Quantity INTEGER," +
                    "Unit_Price DOUBLE," +
                    "PRIMARY KEY (TUID));");

            populateInventory();
        }
    }

    private static void populateInventory() throws SQLException {
        PreparedStatement prep;
        System.out.println("Add records to Inventory_Table table");

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

    public static ResultSet displayInventory() throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, Item_Name, Quantity, Unit_Price FROM Inventory_Table");
        return res;
    }

    public static void resetInventory() throws SQLException {
        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Inventory_Table");
        prep.execute();
        populateInventory();
    }

    public static boolean addInventoryItem(int TUID, String itemName, int quantity, double unitPrice) throws SQLException {
        PreparedStatement prep;

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, TUID);
        prep.setString(2, itemName);
        prep.setInt(3, quantity);
        prep.setDouble(4, unitPrice);
        return prep.execute();
    }

    public static ResultSet getInventoryItemDetails(int TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, Item_Name, Quantity, Unit_Price FROM Inventory_Table WHERE TUID = " + TUID);
        return res;
    }
}