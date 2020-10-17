/*

    Author:     Benjamin J. Dore
    Date:       10/17/2020

    Description:
        This class is where the program starts, it begins by asking the user if they want to reset system to default,
        followed by running an initial file for input. From there, a loop will occur allowing the user to run reports,
        input more files, and close the program.

*/

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Scanner;

public class MainExecution {
    // Entry point.
    public static void main(String[] args) throws FileNotFoundException, SQLException, ClassNotFoundException {
        Scanner userInput = new Scanner(System.in);     // Main user input


        // BELOW CODE IS RUN ON STARTUP ASKING USER TO RESET SYSTEM


        System.out.println("Do you want to reset the Gourmet Grocery Inventory to default? (Y/N)");
        String resetInv = userInput.next().toUpperCase();

        if (resetInv.equals("Y"))
            InventoryDB.resetInventory();   // Reset the inventory to default values if user request this.

        System.out.println("Do you want to reset the Customer and Order tables to empty states? (Y/N)");
        String resetCustAndOrds = userInput.next().toUpperCase();

        if (resetCustAndOrds.equals("Y")) {
            CustomerDB.resetCustomers();                // Empty the customer table
            OrderDB.resetOrders();                      // Empty the orders
            InventoryToOrderDB.resetInventoryToOrder(); // Empty order/inventory associations
        }


        // ABOVE CODE IS RUN ON STARTUP ASKING USER TO RESET SYSTEM


        userInput.nextLine();
        System.out.println("\nDo you want to run the file 'gourmet.txt'? (Y/N)");   // Ask the user to run an intial file.
        String userChoice = userInput.next().toUpperCase();

        // The Scheduler will run and get all the necessary order delivery dates needed when privisioning orders.
        Scheduler.loadCancelledOrderDatesFromDatabaseOnStartup();

        if (userChoice.equals("Y")) {
            TextFileParser.readFile("gourmet.txt");     // Run the 'gourmet.txt' file if user requests this.
            userInput.nextLine();
        }
        else {
            // Have the user run their own file, will print a message if the file has an issue (file doesn't exist).
            try {
                readUserSelectedFile(userInput);
            } catch (Exception ex) {
                System.out.println("\n\nFILE NOT FOUND: Make sure the file is in the same directory as the program");
                System.out.println("\t- RETURNING TO MAIN MENU: Please try to process a file again\n\n");
                userInput.nextLine();
            }
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

            if (userChoice.equals("1")) {
                Reporter.displayCurrentInventory();     // Run the inventory report
            }
            if (userChoice.equals("2")) {
                Reporter.displayCustomerLog();          // Run the customer report.
            }
            if (userChoice.equals("3")) {
                // Get a date range from the user for the Customer Order Summary and Costs report. Then ask if the user
                // wants a simplified report (less details), or a full report (all possible details). If the user doesn't
                // make a valid choice, give message and go back to main options screen.
                String[] dateRanges = getDateRangeFromUser(userInput);
                System.out.print("Do you want the Order Summary or Full Order Details? (S/D): ");
                String sumOrDet = userInput.nextLine().toLowerCase();
                if (sumOrDet.equals("s")) {
                    Reporter.printOrderSummary(dateRanges[0], dateRanges[1]);       // Print summary (simplified info)
                } else if (sumOrDet.equals("d")) {
                    Reporter.printFullOrderSummary(dateRanges[0], dateRanges[1]);   // Print details (all info)
                } else {
                    System.out.println("\n\nNo valid input for order type - cancelling...\n\n");
                }
            }
            if (userChoice.equals("4")) {
                String[] dateRanges = getDateRangeFromUser(userInput);          // Get the date ranges for the report
                Reporter.printOrderDeliveryTimes(dateRanges[0], dateRanges[1]); // Run the order delivery times report
            }
            if (userChoice.equals("5")) {
                String[] dateRanges = getDateRangeFromUser(userInput);          // Get the date ranges for the report
                Reporter.printEmployeeEarnings(dateRanges[0], dateRanges[1]);   // Run employee earnings report
            }
            if (userChoice.equals("6")) {
                try {
                    readUserSelectedFile(userInput);      // Read user file for input
                } catch (Exception ex) {
                    // Print a message if there is an issue reading the file
                    System.out.println("\n\nFILE NOT FOUND: Make sure the file is in the same directory as the program");
                    System.out.println("\t- RETURNING TO MAIN MENU: Please try to process a file again\n\n");
                    userInput.nextLine();
                }
            }
        }
    }

    // Function is responsible for reading a file from the user to be inputted into the system. (Order files, customer files, modification to orders etc.)
    public static void readUserSelectedFile(Scanner userInput) throws FileNotFoundException, SQLException, ClassNotFoundException {
        System.out.print("\nPlease type a file to run (Type 'N' to cancel): ");
        String userFile = userInput.next();
        if (!(userFile.equals("n") || userFile.equals("N"))) {
            TextFileParser.readFile(userFile);      // Text file parser will read the file and perform operations depending on customer adds, new orders, or order modifications.
        } else {
            System.out.println("\n\nCancelling...\n\n");    // User can choose to cancel inputting a file
        }
        if(userInput.hasNextLine()) userInput.nextLine();   // Throw out junky characters so you don't have to hit ENTER twice
    }

    // This function is used to get a date range. These date ranges are used for reports to narrow down focus.
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

            // User cannot enter blank dates, either the system will use defail dates (i.e. earliest/latest delivery dates) or a valid user choice of date.
            beginDate = beginDate.equals("") ? OrderDB.getEarliestDeliveryDate().getString("earliestDeliveryDate") : beginDate;
            endDate = endDate.equals("") ? OrderDB.getLastestDeliveryDate().getString("latestDeliveryDate") : endDate;
        } else {
            // If the user doesn't want to input a date range, then just use the earliest and latest possible delivery dates.
            beginDate = OrderDB.getEarliestDeliveryDate().getString("earliestDeliveryDate");
            endDate = OrderDB.getLastestDeliveryDate().getString("latestDeliveryDate");
        }

        return new String[]{beginDate, endDate};
    }
}
