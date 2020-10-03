import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class MainExecution {
    public static void main(String[] args) throws FileNotFoundException, SQLException, ClassNotFoundException {
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        TextFileParser.readFile("gourmet.txt");
    }
}
