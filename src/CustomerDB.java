import java.sql.*;

public class CustomerDB {

    private static Connection con;

    static {
        con = DatabaseConnection.getDBConnection();
    }

    public static boolean databaseExists() throws SQLException {
        Statement state;
        ResultSet res;

        // Check for database table existence and if it's not there, create it and add 2 records
        state = con.createStatement();
        res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Customer_Table'");

        if (res.next())
            return true;
        else
            return false;
    }

    public static void buildDatabase() throws SQLException {
        Statement state;

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

    public static boolean addCustomer(int TUID, String firstName, String lastName, String phone) throws SQLException, ClassNotFoundException {
        if (!databaseExists())
            buildDatabase();

        PreparedStatement prep;

        prep = con.prepareStatement("INSERT INTO Customer_Table VALUES(?,?,?,?);");
        prep.setInt(1, TUID);
        prep.setString(2, firstName);
        prep.setString(3, lastName);
        prep.setString(4, phone);

        return prep.execute();
    }

    public static ResultSet getCustomers() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, First_Name, Last_Name, Phone FROM Customer_Table");
        return res;
    }

    public static ResultSet getCustomers(int TUID) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, First_Name, Last_Name, Phone FROM Customer_Table WHERE TUID = " + TUID);
        return res;
    }

    public static boolean customerExists(int TUID) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, First_Name, Last_Name, Phone FROM Customer_Table WHERE TUID = " + TUID);
        if (res.next())
            return true;
        else
            return false;
    }

    public static void resetCustomers() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Customer_Table");
        prep.execute();
    }
}
