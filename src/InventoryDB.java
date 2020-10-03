import java.sql.*;

public class InventoryDB {

    private static Connection con;

    static {
        try {
            con = getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static Connection getConnection() throws  SQLException {
        // Database path -- if it's new database, it will be created in the project folder
        return DriverManager.getConnection("jdbc:sqlite:SQLiteGG.db");
    }

    public static boolean databaseExists() throws SQLException, ClassNotFoundException {
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


    public static void buildDatabase() throws ClassNotFoundException, SQLException {
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

    private static void populateInventory() throws SQLException, ClassNotFoundException {
        PreparedStatement prep;
        System.out.println("Add records to Inventory_Table table");

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 101);
        prep.setString(2, "Peanut Butter – Chunky");
        prep.setInt(3, 100);
        prep.setDouble(4, 10.00);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 102);
        prep.setString(2, "Peanut Butter – Smooth");
        prep.setInt(3, 100);
        prep.setDouble(4, 11.00);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 201);
        prep.setString(2, "Jelly – Strawberry");
        prep.setInt(3, 100);
        prep.setDouble(4, 9.50);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 202);
        prep.setString(2, "Jelly – Raspberry");
        prep.setInt(3, 100);
        prep.setDouble(4, 9.50);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 203);
        prep.setString(2, "Jelly – Peach");
        prep.setInt(3, 100);
        prep.setDouble(4, 11.25);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 301);
        prep.setString(2, "Bread – White");
        prep.setInt(3, 40);
        prep.setDouble(4, 17.00);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 302);
        prep.setString(2, "Bread – Wheat");
        prep.setInt(3, 30);
        prep.setDouble(4, 19.00);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 303);
        prep.setString(2, "Bread – Sourdough");
        prep.setInt(3, 40);
        prep.setDouble(4, 21.50);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 401);
        prep.setString(2, "Milk – White");
        prep.setInt(3, 10);
        prep.setDouble(4, 17.00);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 402);
        prep.setString(2, "Milk – Chocolate");
        prep.setInt(3, 15);
        prep.setDouble(4, 17.00);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 403);
        prep.setString(2, "Milk – White 2%");
        prep.setInt(3, 20);
        prep.setDouble(4, 18.00);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO Inventory_Table VALUES(?,?,?,?);");
        prep.setInt(1, 404);
        prep.setString(2, "Milk – Chocolate 2%");
        prep.setInt(3, 20);
        prep.setDouble(4, 18.00);
        prep.execute();
    }

    public static ResultSet displayInventory() throws SQLException, ClassNotFoundException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, Item_Name, Quantity, Unit_Price FROM Inventory_Table");
        return res;
    }

    public static void resetInventory() throws SQLException, ClassNotFoundException {
        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Inventory_Table");
        prep.execute();
    }
}