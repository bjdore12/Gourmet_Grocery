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

            System.out.println("Building the Order_Table");
            state = con.createStatement();
            state.executeUpdate("CREATE TABLE Order_Table(" +
                    "TUID INTEGER," +
                    "Customer_TUID VARCHAR(60)," +
                    "Delivery_Date_Time DATE," +
                    "DeliveryPerson_TUID INTEGER," +
                    "Order_Total DOUBLE," +
                    "PRIMARY KEY (TUID));");

//            state.executeUpdate("CREATE TRIGGER IF NOT EXISTS Add_Order_Details" +
//                    "AFTER INSERT ON Order_Table" +
//                    "BEGIN" +
//                    "" +
//                    "END;");
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

    public static boolean addOrder(int TUID, int Customer_TUID) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        PreparedStatement prep;

        ResultSet orderTotalCost = InventoryToOrderDB.getInventoryToOrderPrice(TUID);

        if (!orderExists(TUID)) {
            prep = con.prepareStatement("INSERT INTO Order_Table VALUES(?,?,?,?,?);");
            prep.setInt(1, TUID);
            prep.setInt(2, Customer_TUID);
            prep.setDouble(5, orderTotalCost.getDouble("total_cost"));
        } else {
            prep = con.prepareStatement("UPDATE Order_Table SET Order_Total = ? WHERE TUID = ?;");
            prep.setDouble(1, orderTotalCost.getDouble("total_cost"));
            prep.setInt(2, TUID);
        }


        return prep.execute();
    }

    public static void resetOrders() throws SQLException {
        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Order_Table");
        prep.execute();
    }


}
