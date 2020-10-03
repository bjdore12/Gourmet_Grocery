import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Scanner;

public class TextFileParser {
    private static File readableFile;
    private static Scanner reader;

    public static void readFile(String filename) throws FileNotFoundException, SQLException, ClassNotFoundException {
        readableFile = new File(filename);
        reader = new Scanner(readableFile);
        String[] fields;

        while(reader.hasNextLine()) {
            String currentLine = reader.nextLine();
            fields = currentLine.split("\t");
            databaseAction(fields);
        }

        reader.close();
    }

    public static void databaseAction(String[] fields) throws SQLException, ClassNotFoundException {
        if (fields[0].equals("C") && !CustomerDB.customerExists(Integer.parseInt(fields[1]))) {
            CustomerDB.addCustomer(Integer.parseInt(fields[1]), fields[2], fields[3], fields[4]);
        }
    }

}
