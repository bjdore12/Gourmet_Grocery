import org.omg.PortableServer.RequestProcessingPolicy;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Scanner;

public class MainExecution {
    public static void main(String[] args) throws FileNotFoundException, SQLException, ClassNotFoundException {
        Scanner userInput = new Scanner(System.in);

        System.out.println("Do you want to reset the Gourmet Grocery Inventory to default? (Y/N)");
        String resetInv = userInput.next().toUpperCase();

        if (resetInv.equals("Y"))
            InventoryDB.resetInventory();

        System.out.println("Do you want to reset the customer and order tables to empty states? (Y/N)");
        String resetCustAndOrds = userInput.next().toUpperCase();

        if (resetCustAndOrds.equals("Y")) {
            CustomerDB.resetCustomers();
            OrderDB.resetOrders();
            InventoryToOrderDB.resetInventoryToOrder();
        }

        userInput.nextLine();
        System.out.println("\nDo you want to run the file 'gourmet.txt'? (Y/N)");
        String userChoice = userInput.next().toUpperCase();

        if (userChoice.equals("Y"))
            TextFileParser.readFile("gourmet.txt");
        else {
            readUserSelectedFile(userInput);
        }

        // Program will loop below, allowing the user to keep choosing options in the program.
        // Program execution ends when the user types the 'Exit' command.

        userInput.nextLine();
        while(!userChoice.equals("exit")) {
            System.out.print("Please type an option as shown below (1/2/3/4/5/6):\n");
            System.out.print("\t1 --> Run Inventory Report (Type 1)\n");
            System.out.print("\t2 --> Run Customer Log Report (Type 2)\n");
            System.out.print("\t3 --> Run Customer Order Summary and Costs Report (Type 3)\n");
            System.out.print("\t4 --> Run Delivery Schedules Report (Type 4)\n");
            System.out.print("\t5 --> Run Employee Earnings (Type 5)\n");
            System.out.print("\t6 --> Process a Transaction File (Type 6)\n");
            System.out.print("\nType 'Exit' to close the program\n");

            userChoice = userInput.nextLine().toLowerCase();

            if (userChoice.equals("1")) {
                InventoryDB.displayCurrentInventory();
            }
            if (userChoice.equals("2")) {
                CustomerDB.displayCustomerLog();
            }
            if (userChoice.equals("3")) {
                Reporter.printFullOrderSummary();
            }
            if (userChoice.equals("4")) {
                // TODO: Must implement function to run Delivery Schedules report
                Reporter.printOrderDeliveryTimes();
            }
            if (userChoice.equals("5")) {
                // TODO: Must implement function to run Employee Earning reports
                System.out.println("COMING SOON - Employee Earnings Report");
            }
            if (userChoice.equals("6")) {
                readUserSelectedFile(userInput);
            }
        }
    }

    public static void readUserSelectedFile(Scanner userInput) throws FileNotFoundException, SQLException, ClassNotFoundException {
        System.out.print("\nPlease type a file to run: ");
        String userFile = userInput.next();
        TextFileParser.readFile(userFile);
        if(userInput.hasNextLine()) userInput.nextLine();
    }
}
