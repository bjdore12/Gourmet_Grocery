import java.sql.*;

public class DeliveryPersonDB {
    private static Connection con;

    static {
        con = DatabaseConnection.getDBConnection();
    }

    public static boolean databaseExists() throws SQLException, ClassNotFoundException {
        Statement state;
        ResultSet res;

        // Check for database table existence and if it's not there, create it and add 2 records
        state = con.createStatement();
        res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='DeliveryPerson_Table'");

        if (res.next())
            return true;
        else
            return false;
    }

    public static void buildDatabase() throws ClassNotFoundException, SQLException {
        Statement state;

        if (!databaseExists()) {

            System.out.println("Building the Customer_Table table");
            state = con.createStatement();
            state.executeUpdate("CREATE TABLE DeliveryPerson_Table(" +
                    "TUID INTEGER," +
                    "Name VARCHAR(60)," +
                    "Pay_Rate DOUBLE," +
                    "PRIMARY KEY (TUID));");

            populateDeliveryPersons();
        }
    }

    private static void populateDeliveryPersons() throws SQLException {

        PreparedStatement prep;
        System.out.println("Add records to Inventory_Table table");

        prep = con.prepareStatement("INSERT INTO DeliveryPerson_Table VALUES(?,?,?);");
        prep.setInt(001, 101);
        prep.setString(2, "Barb");
        prep.setDouble(3, 22.50);
        prep.execute();

        prep = con.prepareStatement("INSERT INTO DeliveryPerson_Table VALUES(?,?,?);");
        prep.setInt(001, 101);
        prep.setString(2, "Alan");
        prep.setDouble(3, 19.00);
        prep.execute();
    }

    public static ResultSet getDeliveryPersons() throws SQLException, ClassNotFoundException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, Name, Pay_Rate,  FROM DeliveryPerson_Table");
        return res;
    }

    public static ResultSet getDeliveryPersons(int TUID) throws SQLException, ClassNotFoundException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, Name, Pay_Rate,  FROM DeliveryPerson_Table WHERE TUID = " + TUID);
        return res;
    }
}
