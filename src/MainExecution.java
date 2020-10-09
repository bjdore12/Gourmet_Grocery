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

        if (userChoice.equals("Y")) {
            TextFileParser.readFile("gourmet.txt");
            userInput.nextLine();
        }
        else {
            readUserSelectedFile(userInput);
        }

        // Program will loop below, allowing the user to keep choosing options in the program.
        // Program execution ends when the user types the 'Exit' command.

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

            // TODO: We need a way (that makes sense) to show the default date ranges for reports that require a date range

            if (userChoice.equals("1")) {
                Reporter.displayCurrentInventory();
            }
            if (userChoice.equals("2")) {
                Reporter.displayCustomerLog();
            }
            if (userChoice.equals("3")) {
                String[] dateRanges = getDateRangeFromUser(userInput);
                System.out.print("Do you want the Order Summary or Full Order Details (S/D):");
                String sumOrDet = userInput.nextLine().toLowerCase();
                if (sumOrDet.equals("s")) {
                    Reporter.printOrderSummary(dateRanges[0], dateRanges[1]);
                } else if (sumOrDet.equals("d")) {
                    Reporter.printFullOrderSummary(dateRanges[0], dateRanges[1]);
                }

            }
            if (userChoice.equals("4")) {
                String[] dateRanges = getDateRangeFromUser(userInput);
                Reporter.printOrderDeliveryTimes(dateRanges[0], dateRanges[1]);
            }
            if (userChoice.equals("5")) {
                String[] dateRanges = getDateRangeFromUser(userInput);
                Reporter.printEmployeeEarnings(dateRanges[0], dateRanges[1]);
            }
            if (userChoice.equals("6")) {
                readUserSelectedFile(userInput);
            }
        }
    }

    public static void readUserSelectedFile(Scanner userInput) throws FileNotFoundException, SQLException, ClassNotFoundException {
        System.out.print("\nPlease type a file to run (Type 'N' to cancel): ");
        String userFile = userInput.next();
        if (!(userFile.equals("n") || userFile.equals("N"))) {
            TextFileParser.readFile(userFile);
            if(userInput.hasNextLine()) userInput.nextLine();
        }
    }

    public static String[] getDateRangeFromUser(Scanner userInput) throws SQLException {
        String beginDate;
        String endDate;

        System.out.print("Do you want to enter a date range for the report? (Y/N): ");
        String inputRange = userInput.nextLine().toUpperCase();

        if (inputRange.equals("Y")) {
            System.out.print("Enter inclusive begin date (YYYY-MM-DD): ");
            beginDate = userInput.nextLine();

            System.out.print("Enter inclusive end date (YYYY-MM-DD): ");
            endDate = userInput.nextLine();

            beginDate = beginDate.equals("") ? OrderDB.getEarliestDeliveryDate().getString("earliestDeliveryDate") : beginDate;
            endDate = endDate.equals("") ? OrderDB.getLastestDeliveryDate().getString("latestDeliveryDate") : endDate;
        } else {
            beginDate = OrderDB.getEarliestDeliveryDate().getString("earliestDeliveryDate");
            endDate = OrderDB.getLastestDeliveryDate().getString("latestDeliveryDate");
        }

        return new String[]{beginDate, endDate};
    }
}
