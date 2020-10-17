/*

    Author:     Benjamin J. Dore
    Date:       10/17/2020

    Description:
        The Reporter class is responsible for generating all report on data in the system.
        The following reports are handled here:
            1. Customer Order Summary and Costs - Orders in the system, the totals for those orders and relevant delivery info
            2. Inventory Report - Displays status info on the current inventory
            3. Employee Earnings - Displays how much delivery person have earned
            4. Delivery Dates - Displays a list of delivery dates for each order and shows delivery person
            5. Customer Log - Display all recorded customers in the system

*/

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;

public class Reporter {

    // TODO: Some of the reports format funny in the command prompt, fix this so you don't lose points on something silly.

    private static Connection con;

    // The Reporter will need to be connected to the DB.
    static {
        con = DatabaseConnection.getDBConnection();
    }

    // Function will print a detailed report on all the orders in the system. Detailed view will include
    // each item in the order, their quantity, and costs.
    public static void printOrderSpecifics(int Order_TUID) throws SQLException {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();

        Statement state;
        ResultSet res;
        ResultSet itemDetails;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID);

        System.out.printf("\t%-5s %-25s %-9s %s\n", "ID", "Item", "Quantity", "Unit Price");

        while (res.next()) {
            itemDetails = InventoryDB.getInventoryItemDetails(res.getInt("Inventory_TUID"));
            System.out.printf("\t%-5s %-25s %-9s %s\n",
                    formatTUID(res.getInt("Inventory_TUID")),
                    itemDetails.getString("Item_Name"),
                    res.getInt("Quantity"),
                    formatter.format(res.getDouble("Inventory_Unit_Price")));
        }
        System.out.println();
    }

    // Report will print a summary of orders and the details for each order including each item, quantites and costs.
    public static void printFullOrderSummary(String beginDate, String endDate) throws SQLException {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        ResultSet orderSummary = OrderDB.getOrders(beginDate, endDate, "Customer_TUID");
        ResultSet totalSales = OrderDB.getAllOrderTotalCost(beginDate, endDate);
        ResultSet customerInfo;

        System.out.println("\n-------------------------------------------");
        System.out.println("| Customer Order Summary and Costs Report |");
        System.out.println("-------------------------------------------");
        System.out.println("From: " + beginDate + " To: " + endDate);
        System.out.println("Grand Total Order Costs: " + formatter.format(totalSales.getDouble("totalSales")));
        while(orderSummary.next()) {
            customerInfo = CustomerDB.getCustomers(orderSummary.getInt("Customer_TUID"));
            int orderQuantity = InventoryToOrderDB.getOrderTotalQuantity(orderSummary.getInt("TUID"));
            ResultSet deliveryPerson = DeliveryPersonDB.getDeliveryPersons(orderSummary.getInt("DeliveryPerson_TUID"));
            System.out.printf("\n%-9s %-15s %-15s %-15s %-9s %-20s %-17s %-9s\n", "Order ID", "Cust. First Nm", "Cust. Last Nm", "Phone", "Quantity", "Delivery Date/Time", "Delivery Person", "Order Total");
            System.out.printf("%-9s %-15s %-15s %-15s %-9s %-20s %-17s %-9s\n",
                    formatTUID(orderSummary.getInt("TUID")),
                    customerInfo.getString("First_Name"),
                    customerInfo.getString("Last_Name"),
                    customerInfo.getString("Phone"),
                    orderQuantity,
                    orderSummary.getString("Delivery_Date_Time"),
                    deliveryPerson.getString("Name"),
                    formatter.format(orderSummary.getDouble("Order_Total")));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            printOrderSpecifics(orderSummary.getInt("TUID"));
        }
    }

    // Prints a report of all delivery times in the system for each order, includes delivery person.
    public static void printOrderDeliveryTimes() throws SQLException {
        ResultSet orderSummary = OrderDB.getOrders();
        ResultSet customerInfo;

        System.out.println("\n-----------------------------");
        System.out.println("| Delivery Schedules Report |");
        System.out.println("-----------------------------\n");
        System.out.printf("\n%-9s %-15s %-15s %-20s %-17s\n", "Order ID", "Cust. First Nm", "Cust. Last Nm", "Delivery Date/Time", "Delivery Person");
        System.out.println("---------------------------------------------------------------------------");
        while(orderSummary.next()) {
            customerInfo = CustomerDB.getCustomers(orderSummary.getInt("Customer_TUID"));
            int orderQuantity = InventoryToOrderDB.getOrderTotalQuantity(orderSummary.getInt("TUID"));
            ResultSet deliveryPerson = DeliveryPersonDB.getDeliveryPersons(orderSummary.getInt("DeliveryPerson_TUID"));

            System.out.printf("%-9s %-15s %-15s %-20s %-17s\n",
                    formatTUID(orderSummary.getInt("TUID")),
                    customerInfo.getString("First_Name"),
                    customerInfo.getString("Last_Name"),
                    orderSummary.getString("Delivery_Date_Time"),
                    deliveryPerson.getString("Name"));
        }
        System.out.println("---------------------------------------------------------------------------");
        System.out.println();
    }

    // Prints a report of all delivery times within a date range, for each order, includes delivery person.
    public static void printOrderDeliveryTimes(String beginDate, String endDate) throws SQLException {
        ResultSet orderSummary = OrderDB.getOrders(beginDate, endDate, "TUID");
        ResultSet customerInfo;

        System.out.println("\n-----------------------------");
        System.out.println("| Delivery Schedules Report |");
        System.out.println("-----------------------------");
        System.out.println("From: " + beginDate + " To: " + endDate);
        System.out.printf("\n%-9s %-15s %-15s %-20s %-17s\n", "Order ID", "Cust. First Nm", "Cust. Last Nm", "Delivery Date/Time", "Delivery Person");
        System.out.println("---------------------------------------------------------------------------");
        while(orderSummary.next()) {
            customerInfo = CustomerDB.getCustomers(orderSummary.getInt("Customer_TUID"));
            int orderQuantity = InventoryToOrderDB.getOrderTotalQuantity(orderSummary.getInt("TUID"));
            ResultSet deliveryPerson = DeliveryPersonDB.getDeliveryPersons(orderSummary.getInt("DeliveryPerson_TUID"));

            System.out.printf("%-9s %-15s %-15s %-20s %-17s\n",
                    formatTUID(orderSummary.getInt("TUID")),
                    customerInfo.getString("First_Name"),
                    customerInfo.getString("Last_Name"),
                    orderSummary.getString("Delivery_Date_Time"),
                    deliveryPerson.getString("Name"));
        }
        System.out.println("---------------------------------------------------------------------------");
        System.out.println();
    }

    // Intended to print a high level overview of all orders within a date range, there is less information in this report than the
    // full order summary function.
    public static void printOrderSummary(String beginDate, String endDate) throws SQLException {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        ResultSet orderSummary = OrderDB.getOrders(beginDate, endDate, "Customer_TUID");
        ResultSet totalSales = OrderDB.getAllOrderTotalCost(beginDate, endDate);
        ResultSet customerInfo;

        System.out.println("\n-------------------------------------------");
        System.out.println("| Customer Order Summary and Costs Report |");
        System.out.println("-------------------------------------------");
        System.out.println("From: " + beginDate + " To: " + endDate);
        System.out.printf("\n%-9s %-15s %-15s %-15s %-9s %-20s %-17s %-9s\n", "Order ID", "Cust. First Nm", "Cust. Last Nm", "Phone", "Quantity", "Delivery Date/Time", "Delivery Person", "Order Total");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");

        while(orderSummary.next()) {
            customerInfo = CustomerDB.getCustomers(orderSummary.getInt("Customer_TUID"));
            int orderQuantity = InventoryToOrderDB.getOrderTotalQuantity(orderSummary.getInt("TUID"));
            ResultSet deliveryPerson = DeliveryPersonDB.getDeliveryPersons(orderSummary.getInt("DeliveryPerson_TUID"));
            System.out.printf("%-9s %-15s %-15s %-15s %-9s %-20s %-17s %-9s\n",
                    formatTUID(orderSummary.getInt("TUID")),
                    customerInfo.getString("First_Name"),
                    customerInfo.getString("Last_Name"),
                    customerInfo.getString("Phone"),
                    orderQuantity,
                    orderSummary.getString("Delivery_Date_Time"),
                    deliveryPerson.getString("Name"),
                    formatter.format(orderSummary.getDouble("Order_Total")));
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        System.out.println("\tGrand Total: " + formatter.format(totalSales.getDouble("totalSales")));
        System.out.println();
    }

    // Prints a report of how much an employee earend for each day within a date range. Prints total earning for employees.
    public static void printEmployeeEarnings(String beginDate, String endDate) throws SQLException {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        ResultSet empDailyEarnings = OrderDB.getEmpPayPerDelivery(beginDate, endDate);
        ResultSet empTotalEarnings = OrderDB.getEmpTotalPay(beginDate, endDate);
        ResultSet allTotalPay = OrderDB.getEmpAggregateTotalPay(beginDate, endDate);

        System.out.println("\n---------------------");
        System.out.println("| Employee Earnings |");
        System.out.println("---------------------");
        System.out.println("From: " + beginDate + " To: " + endDate);
        System.out.printf("\n%-15s %-15s %-15s\n", "Date", "Employee", "Daily Pay");
        System.out.println("-----------------------------------------------------");

        while(empDailyEarnings.next()) {
            System.out.printf("%-15s %-15s %-15s\n",
                    empDailyEarnings.getString("orderDate"),
                    empDailyEarnings.getString("empName"),
                    formatter.format(empDailyEarnings.getDouble("dailyPay")));
        }
        System.out.println("-----------------------------------------------------");
        while (empTotalEarnings.next()) {
            System.out.print("\tEmployee: " +
                    empTotalEarnings.getString("empName") + " --> Earnings: " +
                    formatter.format(empTotalEarnings.getDouble("dailyPay")) +
                    "\n");
        }
        System.out.println("\t-------------------------");
        while (allTotalPay.next()) {
            System.out.print("\tTotal Earnings:\t" +
                    formatter.format(allTotalPay.getDouble("dailyPay")) +
                    "\n");
        }
        System.out.println();
    }

    // Print an inventory status report showing remaining quantities.
    public static void displayCurrentInventory() {
        ResultSet rs;
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        try {
            rs = InventoryDB.displayInventory();

            System.out.println("\n--------------------");
            System.out.println("| Inventory Report |");
            System.out.println("--------------------");
            System.out.printf("%-5s %-25s %-9s %s\n", "TUID", "Item", "Quantity", "Unit Price");
            System.out.println("-------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5s %-25s %-9s %s\n", formatTUID(rs.getInt("TUID")),
                        rs.getString("Item_Name"),
                        rs.getString("Quantity"),
                        formatter.format(rs.getDouble("Unit_Price")));
            }
            System.out.println("-------------------------------------------------------------\n");

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Displays all the customers that have placed orders in the system.
    public static void displayCustomerLog() {
        ResultSet rs;
        try {

            rs = CustomerDB.getCustomers();

            System.out.println("\n-----------------------");
            System.out.println("| Customer Log Report |");
            System.out.println("-----------------------");
            System.out.printf("%-5s %-12s %-12s %s\n", "TUID", "First Name", "Last Name", "Phone");
            System.out.println("-------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5s %-12s %-12s %s\n", formatTUID(rs.getInt("TUID")),
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

    // Used to format the TUID (So if TUID is 1, then display as 001).
    public static String formatTUID(int TUID) {
        final int THREE_DIGITS = 3;
        int length = String.valueOf(TUID).length();
        String formattedTUID = TUID + "";

        while (length < THREE_DIGITS) {
            formattedTUID = "0" + formattedTUID;
            length++;
        }

        return formattedTUID;
    }
}
