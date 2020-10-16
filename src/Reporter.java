import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;

public class Reporter {

    // TODO: Some of the reports format funny in the command prompt, fix this so you don't lose points on something silly.

    private static Connection con;

    static {
        con = DatabaseConnection.getDBConnection();
    }

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

    public static void printFullOrderSummary(String beginDate, String endDate) throws SQLException, ClassNotFoundException {
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

    public static void printOrderDeliveryTimes() throws SQLException, ClassNotFoundException {
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

    public static void printOrderDeliveryTimes(String beginDate, String endDate) throws SQLException, ClassNotFoundException {
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
        System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tGrand Total: " + formatter.format(totalSales.getDouble("totalSales")));
        System.out.println();
    }

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
                    empTotalEarnings.getString("empName") + " -->\tEarnings:\t\t" +
                    formatter.format(empTotalEarnings.getDouble("dailyPay")) +
                    "\n");
        }
        System.out.println("\t\t\t\t\t\t-------------------------");
        while (allTotalPay.next()) {
            System.out.print("\t\t\t\t\t\tTotal Earnings:\t" +
                    formatter.format(allTotalPay.getDouble("dailyPay")) +
                    "\n");
        }
        System.out.println();
    }

    public static void displayCurrentInventory() {
        ResultSet rs;
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        try {
            // Bring back the set of user from the database
            rs = InventoryDB.displayInventory();
            // Iterate over the resultset, print out each record's details
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

    public static void displayCustomerLog() {
        ResultSet rs;
        try {
            // Bring back the set of user from the database
            rs = CustomerDB.getCustomers();
            // Iterate over the resultset, print out each record's details

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
