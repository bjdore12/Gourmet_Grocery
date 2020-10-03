import java.sql.ResultSet;
import java.util.Scanner;

public class MainExecution {
    public static void main(String[] args) {
        ResultSet rs;

        Scanner userInput = new Scanner(System.in);
        System.out.println("Please choose an option");

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

            InventoryDB.resetInventory();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
