import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class MainExecution {
    public static void main(String[] args) throws FileNotFoundException, SQLException, ClassNotFoundException {
        ResultSet rs;
        Scanner userInput = new Scanner(System.in);

        System.out.println("Do you want to reset the Gourmet Grocery Inventory to default? (Y/N)");
        String resetInv = userInput.next();

        if (resetInv.equals("Y"))
            InventoryDB.resetInventory();

        try {
            InventoryDB.buildDatabase();
            // Bring back the set of user from the database
            rs = InventoryDB.displayInventory();
            // Iterate over the resultset, print out each record's details
            while (rs.next()) {
                System.out.println(rs.getInt("TUID") + " " +
                        rs.getString("Item_Name") + " " +
                        rs.getString("Quantity") + " " +
                        rs.getString("Unit_Price"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Do you want to reset the customer and order tables to empty states? (Y/N)");
        String resetCustAndOrds = userInput.next();

        if (resetCustAndOrds.equals("Y")) {
            CustomerDB.resetCustomers();
            OrderDB.resetOrders();
            InventoryToOrderDB.resetInventoryToOrder();
        }


        userInput.nextLine();
        System.out.println("\nDo you want to run the file 'gourmet.txt'? (Y/N)");
        String userChoice = userInput.next();

        if (userChoice.equals("Y"))
            TextFileParser.readFile("gourmet.txt");
        else {
            System.out.print("\nPlease type a file to run: ");
            String userFile = userInput.next();
            TextFileParser.readFile(userFile);
        }
    }
}
