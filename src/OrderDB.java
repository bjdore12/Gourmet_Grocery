/*

    Author:     Benjamin J. Dore
    Date:       10/17/2020

    Description:
        This class defines the Order_Table and contains information on order details like customer, order #, delivery date,
        delivery person, and total order costs. This is a high level table used in conjunction with the Inventory_To_Order table

*/

import java.sql.*;

public class OrderDB {
    private static Connection con;

    // Call the database connection and get connected to the DB.
    static {
        con = DatabaseConnection.getDBConnection();
    }

    // Does the database already exist? Check the sqlite_master table for the Order_Table
    public static boolean databaseExists() throws SQLException {
        Statement state;
        ResultSet res;

        // Check for database table existence and if it's not there, create it.
        state = con.createStatement();
        res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Order_Table'");

        if (res.next())     // If the table doesn't exist, this will return false.
            return true;
        else
            return false;
    }

    // Build the Order_Table database
    public static void buildDatabase() throws SQLException {
        Statement state;

        // Check to see if the database already exists, only create the table if it isn't already there.
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

    // Checks to see if an order already exists.
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

    // Create a new order and add it to the Order_Table
    public static boolean addOrder(int TUID, int Customer_TUID) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        PreparedStatement prep;

        ResultSet orderTotalCost = InventoryToOrderDB.getInventoryToOrderPrice(TUID);   // Get the total costs for an order.

        if (!orderExists(TUID)) {       // Add the order to the database if it doesn't exist.
            prep = con.prepareStatement("INSERT INTO Order_Table VALUES(?,?,?,?,?);");
            prep.setInt(1, TUID);
            prep.setInt(2, Customer_TUID);
            prep.setString(3, Scheduler.nextDeliveryTime());
            prep.setInt(4, Scheduler.assignDeliveryPerson());
            prep.setDouble(5, orderTotalCost.getDouble("total_cost"));
        } else {
            // If the order does exist, modify it instead.
            prep = con.prepareStatement("UPDATE Order_Table SET Order_Total = ? WHERE TUID = ?;");
            prep.setDouble(1, orderTotalCost.getDouble("total_cost"));
            prep.setInt(2, TUID);
        }

        return prep.execute();
    }

    // Get all of the orders in the Order_Table
    public static ResultSet getOrders() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Order_Table GROUP BY TUID");
        return res;
    }

    // Get all of the orders in the Order_Table within a specific date range. Can also choose to sort by a specific field.
    public static ResultSet getOrders(String beginDate, String endDate, String sortCriteria) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Order_Table WHERE DATE(\"Delivery_Date_Time\") >= DATE('" + beginDate + "') AND DATE(\"Delivery_Date_Time\") <= DATE('" + endDate + "') GROUP BY TUID ORDER BY " + sortCriteria);
        return res;
    }

    // Get a specific order from the Order_Table
    public static ResultSet getOrders(int TUID) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Order_Table WHERE TUID = " + TUID);
        return res;
    }

    // Get the costs for all the orders in the Order_Table within a specific date range.
    public static ResultSet getAllOrderTotalCost(String beginDate, String endDate) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT SUM(Order_Total) AS totalSales \n" +
                "FROM Order_Table \n" +
                "WHERE DATE(Delivery_Date_Time) >= DATE('"+beginDate+"') AND DATE(\"Delivery_Date_Time\") <= DATE('"+endDate+"');");
        return res;
    }

    // Get all the delivery dates that exist in the Order_Table
    public static ResultSet getAllDeliveryDates() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT DATE(Delivery_Date_Time) AS deliveryDate\n" +
                "FROM Order_Table\n" +
                "GROUP BY DATE(Delivery_Date_Time)");
        return res;
    }

    // Get all the filled delivery times for a specific date.
    public static ResultSet getDeliveryTimesForADate(String date) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
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

    // Get all the filled delivery times for a specific date and also get the associated delivery person.
    public static ResultSet getDeliveryTimesForADateWithDeliveryPerson(String date, String time) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
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

    // Get a count of delivery times for a specific date. This is used to figure out if there are time slots that are open
    // on a particular date.
    public static ResultSet getDeliveryTimesForADate(String date, String time) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
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

    // Get the earliest delivery date from the Order_Table
    public static ResultSet getEarliestDeliveryDate() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT DATE(MIN(Delivery_Date_Time)) AS earliestDeliveryDate FROM Order_Table;");
        return res;
    }

    // Get the latest delivery date from the Order_Table
    public static ResultSet getLastestDeliveryDate() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT DATE(MAX(Delivery_Date_Time)) AS latestDeliveryDate FROM Order_Table;");
        return res;
    }

    // Get the latest delivery time, function is intended to be used with the Scheduler to find the last delivery time for a date.
    public static ResultSet getLastestDeliveryTime() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        Statement state;
        ResultSet res;

        state = con.createStatement();
        res = state.executeQuery("SELECT TIME(MAX(Delivery_Date_Time)) AS latestDeliveryTime FROM Order_Table;");
        return res;
    }

    // Get all the time slots for the latest delivery date.
    public static ResultSet getLastestDeliveryDateTimeSlots() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        // Get the latest delivery date
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

    // Return a ResultSet with data on how much an employee earned within a date range (for each day).
    public static ResultSet getEmpPayPerDelivery(String beginDate, String endDate) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
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

    // Return a ResultSet with data on how much an employee earned (for each day).
    public static ResultSet getEmpPayPerDelivery() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
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

    // Get the total amount each employee earned within a date range.
    public static ResultSet getEmpTotalPay(String beginDate, String endDate) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
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

    // Get the total amount of money earned for all employees (So how much earned between Barb and Alan combined).
    public static ResultSet getEmpAggregateTotalPay(String beginDate, String endDate) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
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

    // Reset the Order_Table to empty, to be used on startup when the user wants to reset the system.
    public static void resetOrders() throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Order_Table");
        prep.execute();
    }

    // Delete an order from the Order_Table
    public static boolean deleteOrder(int TUID) throws SQLException {
        if (!databaseExists())      // Check to see if the table exists first.
            buildDatabase();

        ResultSet orderDetails = getOrders(TUID);       // Get the order details.
        // Have the scheduler cancel the order, and save the cancelled date for use later.
        Scheduler.cancelDelivery(orderDetails.getString("Delivery_Date_Time"), orderDetails.getInt("DeliveryPerson_TUID"));

        PreparedStatement prep;

        prep = con.prepareStatement("DELETE FROM Order_Table WHERE TUID = ?");
        prep.setInt(1, TUID);
        return prep.execute();
    }
}
