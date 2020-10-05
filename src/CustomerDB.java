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

    public static void displayCustomerLog() {
        ResultSet rs;
        try {
            // Bring back the set of user from the database
            rs = getCustomers();
            // Iterate over the resultset, print out each record's details

            System.out.printf("\n%-5s %-12s %-12s %s\n", "TUID", "First Name", "Last Name", "Phone");
            System.out.println("-------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d %-12s %-12s %s\n", rs.getInt("TUID"),
                        rs.getString("First_Name"),
                        rs.getString("Last_Name"),
                        rs.getString("Phone"));
            }
            System.out.println("-------------------------------------------------------------\n");

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
