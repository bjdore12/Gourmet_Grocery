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

    public static ResultSet getOrders(String beginDate, String endDate, String sortCriteria) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Order_Table WHERE DATE(\"Delivery_Date_Time\") >= DATE('" + beginDate + "') AND DATE(\"Delivery_Date_Time\") <= DATE('" + endDate + "') GROUP BY TUID ORDER BY " + sortCriteria);
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

    public static ResultSet getAllOrderTotalCost(String beginDate, String endDate) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT SUM(Order_Total) AS totalSales \n" +
                "FROM Order_Table \n" +
                "WHERE DATE(Delivery_Date_Time) >= DATE('"+beginDate+"') AND DATE(\"Delivery_Date_Time\") <= DATE('"+endDate+"');");
        return res;
    }

    public static ResultSet getAllDeliveryDates() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT DATE(Delivery_Date_Time) AS deliveryDate\n" +
                "FROM Order_Table\n" +
                "GROUP BY DATE(Delivery_Date_Time)");
        return res;
    }

    public static ResultSet getDeliveryTimesForADate(String date) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TIME(Delivery_Date_Time) AS TIME, DeliveryPerson_TUID\n" +
                "FROM Order_Table\n" +
                "WHERE DATE(Delivery_Date_Time) = DATE('"+date+"')\n" +
                "ORDER BY TIME(Delivery_Date_Time)");
        return res;
    }

    public static ResultSet getDeliveryTimesForADateWithDeliveryPerson(String date, String time) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TIME(Delivery_Date_Time) AS TIME, DeliveryPerson_TUID\n" +
                "FROM Order_Table\n" +
                "WHERE DATE(Delivery_Date_Time) = DATE('"+date+"')\n" +
                "AND TIME(Delivery_Date_Time) = TIME('"+time+"')\n" +
                "ORDER BY TIME(Delivery_Date_Time)");
        return res;
    }

    public static ResultSet getDeliveryTimesForADate(String date, String time) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT COUNT(TIME(Delivery_Date_Time)) AS Time_Count\n" +
                "FROM Order_Table\n" +
                "WHERE DATE(Delivery_Date_Time) = DATE('"+date+"') AND TIME(Delivery_Date_Time) = TIME('"+time+"')\n" +
                "ORDER BY TIME(Delivery_Date_Time)");
        return res;
    }

    public static ResultSet getEarliestDeliveryDate() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT DATE(MIN(Delivery_Date_Time)) AS earliestDeliveryDate FROM Order_Table;");
        return res;
    }


    public static ResultSet getLastestDeliveryDate() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT DATE(MAX(Delivery_Date_Time)) AS latestDeliveryDate FROM Order_Table;");
        return res;
    }

    public static ResultSet getLastestDeliveryTime() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TIME(MAX(Delivery_Date_Time)) AS latestDeliveryTime FROM Order_Table;");
        return res;
    }

    public static ResultSet getLastestDeliveryDateTimeSlots() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        String latestDate = getLastestDeliveryDate().getString("latestDeliveryDate");

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TIME(Delivery_Date_Time) As deliveryTime, COUNT(TIME(Delivery_Date_Time)) AS count, DATE(Delivery_Date_Time) AS deliveryDate\n" +
                "FROM Order_Table\n" +
                "WHERE DATE(Delivery_Date_Time) == DATE('"+latestDate+"')\n" +
                "GROUP BY TIME(Delivery_Date_Time);");
        return res;
    }

    public static ResultSet getEmpPayPerDelivery(String beginDate, String endDate) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT DATE(Delivery_Date_Time) AS orderDate, Name As empName, SUM(Pay_Rate) As dailyPay\n" +
                "FROM Order_Table, DeliveryPerson_Table\n" +
                "WHERE Order_Table.DeliveryPerson_TUID = DeliveryPerson_Table.TUID\n" +
                "AND (DATE(Delivery_Date_Time) >= DATE('"+ beginDate +"')) AND (DATE(Delivery_Date_Time) <= DATE('"+ endDate +"'))\n" +
                "GROUP BY orderDate, Name ORDER BY Name;;");

        return res;
    }

    public static ResultSet getEmpPayPerDelivery() throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT DATE(Delivery_Date_Time) AS orderDate, Name As empName, SUM(Pay_Rate) As dailyPay\n" +
                "FROM Order_Table, DeliveryPerson_Table\n" +
                "WHERE Order_Table.DeliveryPerson_TUID = DeliveryPerson_Table.TUID\n" +
                "GROUP BY orderDate, Name;");

        return res;
    }

    public static ResultSet getEmpTotalPay(String beginDate, String endDate) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT Name As empName, SUM(Pay_Rate) As dailyPay\n" +
                "FROM Order_Table, DeliveryPerson_Table\n" +
                "WHERE Order_Table.DeliveryPerson_TUID = DeliveryPerson_Table.TUID\n" +
                "AND (DATE(Delivery_Date_Time) >= DATE('"+ beginDate +"')) AND (DATE(Delivery_Date_Time) <= DATE('"+ endDate +"'))\n" +
                "GROUP BY Name;");

        return res;
    }

    public static ResultSet getEmpAggregateTotalPay(String beginDate, String endDate) throws SQLException {
        if (!databaseExists())
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT SUM(Pay_Rate) As dailyPay\n" +
                "FROM Order_Table, DeliveryPerson_Table\n" +
                "WHERE Order_Table.DeliveryPerson_TUID = DeliveryPerson_Table.TUID\n" +
                "AND (DATE(Delivery_Date_Time) >= DATE('"+ beginDate +"')) AND (DATE(Delivery_Date_Time) <= DATE('"+ endDate +"'));\n");

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

    public static void main(String[] args) throws SQLException {
        ResultSet res = getAllDeliveryDates();
        while(res.next()) {
            ResultSet timeOne = getDeliveryTimesForADate(res.getString("deliveryDate"), "09:00:00");
            ResultSet timeTwo = getDeliveryTimesForADate(res.getString("deliveryDate"), "11:00:00");
            ResultSet timeThree = getDeliveryTimesForADate(res.getString("deliveryDate"), "14:00:00");
            ResultSet timeFour = getDeliveryTimesForADate(res.getString("deliveryDate"), "16:00:00");

            System.out.println(res.getString("deliveryDate"));
            System.out.println("\t" + timeOne.getString("Time_Count"));
            System.out.println("\t" + timeTwo.getString("Time_Count"));
            System.out.println("\t" + timeThree.getString("Time_Count"));
            System.out.println("\t" + timeFour.getString("Time_Count"));
        }
    }
}
