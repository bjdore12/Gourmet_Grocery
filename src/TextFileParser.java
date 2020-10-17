/*

    Author:     Benjamin J. Dore
    Date:       10/17/2020

    Description:
        The TextFileParser handles reading input files from the user and performing actions on the DBs. This class
        handles adding customers, creating orders and modifying existing orders.
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Scanner;

public class TextFileParser {

    private static File readableFile;   // The users file
    private static Scanner reader;      // Scanner to read the file

    // Takes the users file and reads it and performs database actions.
    public static void readFile(String filename) throws FileNotFoundException, SQLException, ClassNotFoundException {
        readableFile = new File(filename);
        reader = new Scanner(readableFile);
        String[] fields;

        while(reader.hasNextLine()) {
            String currentLine = reader.nextLine();
            fields = currentLine.split("\t");
            databaseAction(fields);     // Perform a DB action on the line.
        }
        reader.close();
    }

    // Database actions include, adding new customers to the DB, creating new orders, and modifying existing orders.
    public static void databaseAction(String[] fields) throws SQLException {
        if (fields[0].equals("C") && !CustomerDB.customerExists(Integer.parseInt(fields[1]))) {
            CustomerDB.addCustomer(Integer.parseInt(fields[1]), fields[2], fields[3], fields[4]);
        }
        if (fields[0].equals("O")) {
            InventoryToOrderDB.addOrderToInventory(Integer.parseInt(fields[2]), Integer.parseInt(fields[3]), Integer.parseInt(fields[4]));
            OrderDB.addOrder(Integer.parseInt(fields[2]), Integer.parseInt(fields[1]));
            InventoryToOrderDB.deleteInvToOrderAssocationIfZeroQuantity(Integer.parseInt(fields[2]));
        }
        if (fields[0].equals("M")) {
            InventoryToOrderDB.modifyOrderToInventory(Integer.parseInt(fields[2]), Integer.parseInt(fields[3]), Integer.parseInt(fields[4]));
            OrderDB.addOrder(Integer.parseInt(fields[2]), Integer.parseInt(fields[1]));
            InventoryToOrderDB.deleteInvToOrderAssocationIfZeroQuantity(Integer.parseInt(fields[2]));
        }
    }
}
