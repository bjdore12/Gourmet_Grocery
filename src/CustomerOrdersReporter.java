import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerOrdersReporter {

    private static Connection con;

    static {
        con = DatabaseConnection.getDBConnection();
    }

    public static void printOrderSpecifics(int Order_TUID) throws SQLException {
        Statement state;
        ResultSet res;
        ResultSet itemDetails;

        state = con.createStatement();
        res = state.executeQuery("SELECT * FROM Inventory_To_Order_Table WHERE ORDER_TUID = " + Order_TUID);

        System.out.printf("\t%-5s %-25s %-9s %s\n", "ID", "Item", "Quantity", "Unit Price");

        while (res.next()) {
            itemDetails = InventoryDB.getInventoryItemDetails(res.getInt("Inventory_TUID"));
            System.out.printf("\t%-5d %-25s %-9s %s\n", res.getInt("Inventory_TUID"), itemDetails.getString("Item_Name"), res.getInt("Quantity"), res.getDouble("Inventory_Unit_Price"));
        }
        System.out.println();
    }

    public static void printFullOrderSummary() throws SQLException {
        ResultSet orderSummary = OrderDB.getOrders();
        ResultSet customerInfo;

        while(orderSummary.next()) {
            customerInfo = CustomerDB.getCustomers(orderSummary.getInt("Customer_TUID"));
            int orderQuantity = InventoryToOrderDB.getOrderTotalQuantity(orderSummary.getInt("TUID"));
            System.out.printf("\n%-9s %-15s %-15s %-15s %-9s %-9s\n", "Order ID", "Customer - FN", "Customer - LN", "Phone", "Quantity", "Order Total");

            System.out.printf("%-9d %-15s %-15s %-15s %-9s %-9s\n",
                    orderSummary.getInt("TUID"),
                    customerInfo.getString("First_Name"),
                    customerInfo.getString("Last_Name"),
                    customerInfo.getString("Phone"),
                    orderQuantity,
                    orderSummary.getDouble("Order_Total"));
            System.out.println("---------------------------------------------------------------------------------");
            printOrderSpecifics(orderSummary.getInt("TUID"));
        }
    }

    public static void printOrderSummary() {
        // TODO: Make a more simplified version of the order reports
    }

    public static void main(String[] args) throws SQLException {
        //printOrderSpecifics(1);
        printFullOrderSummary();
    }
}
