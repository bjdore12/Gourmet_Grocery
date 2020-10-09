import java.sql.*;

public class OrderDB {
    private static Connection con;

    static {
        con = DatabaseConnection.getDBConnection();
    }

    public static boolean databaseExists() throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Order_Table'");

        if (res.next())
            return true;
        else
            return false;
    }

    public static void buildDatabase() throws SQLException {
        Statement state;

        if (!databaseExists()) {

            state = con.createStatement();
            state.executeUpdate("CREATE TABLE Order_Table(" +
                    "TUID INTEGER," +
                    "Customer_TUID VARCHAR(60)," +
                    "Delivery_Date_Time VARCHAR(60)," +
                    "DeliveryPerson_TUID INTEGER," +
                    "Order_Total DOUBLE," +
                    "PRIMARY KEY (TUID));");

        }
    }

    public static boolean orderExists(int TUID) throws SQLException {
        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TUID, Customer_TUID, Delivery_Date_Time, DeliveryPerson_TUID, Order_Total FROM Order_Table WHERE TUID = " + TUID);
        if (res.next())
            return true;
        else
            return false;
    }

    public static boolean addOrder(int TUID, int Customer_TUID) throws SQLException, ClassNotFoundException {
        if (!databaseExists())
            buildDatabase();

        PreparedStatement prep;

        ResultSet orderTotalCost = InventoryToOrderDB.getInventoryToOrderPrice(TUID);

        if (!orderExists(TUID)) {
            prep = con.prepareStatement("INSERT INTO Order_Table VALUES(?,?,?,?,?);");
            prep.setInt(1, TUID);
            prep.setInt(2, Customer_TUID);
            prep.setString(3, Scheduler.nextDeliveryTime());
            prep.setInt(4, Scheduler.assignDeliveryPerson());
            prep.setDouble(5, orderTotalCost.getDouble("total_cost"));
        } else {
            prep = con.prepareStatement("UPDATE Order_Table SET Order_Total = ? WHERE TUID = ?;");
            prep.setDouble(1, orderTotalCost.getDouble("total_cost"));
            prep.setInt(2, TUID);
        }

        return prep.execute();
    }

    public static ResultSet getOrders() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Order_Table GROUP BY TUID");
        return res;
    }

    public static ResultSet getOrders(String beginDate, String endDate) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Order_Table WHERE DATE(\"Delivery_Date_Time\") >= DATE('" + beginDate + "') AND DATE(\"Delivery_Date_Time\") <= DATE('" + endDate + "') GROUP BY TUID");
        return res;
    }

    public static ResultSet getOrders(int TUID) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Order_Table WHERE TUID = " + TUID);
        return res;
    }

    public static ResultSet getEarliestDeliveryDate() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT MIN(Delivery_Date_Time) AS earliestDeliveryDate FROM Order_Table;");
        return res;
    }

    public static ResultSet getLastestDeliveryDate() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT MAX(Delivery_Date_Time) AS latestDeliveryDate FROM Order_Table;");
        return res;
    }

    public static void resetOrders() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Order_Table");
        prep.execute();
    }

    public static boolean deleteOrder(int TUID) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        ResultSet orderDetails = getOrders(TUID);
        Scheduler.cancelDelivery(orderDetails.getString("Delivery_Date_Time"), orderDetails.getInt("DeliveryPerson_TUID"));

        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Order_Table WHERE TUID = ?");
        prep.setInt(1, TUID);
        return prep.execute();
    }
}
