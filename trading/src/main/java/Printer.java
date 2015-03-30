import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for producing the output file.
 */
public class Printer {

    private static final Logger logger = Logger.getLogger("log");
    private static final String OUTPUT_FILE_NAME = "orders.csv";

    /**
     * Appends orders to a csv file.
     * @param orders An ArrayList of Orders.
     */
    public static void printOrders(List<Order> orders) {
        FileWriter file;
        try {
            file = new FileWriter(OUTPUT_FILE_NAME);
        } catch (IOException e) {
            logger.severe(e.getMessage());
            return;
        }
        CSVWriter writer = new CSVWriter(file, ',');
        for (Order o : orders){
            writer.writeNext(o.toStringArray());
        }
        logger.log(Level.INFO, "\nFinished.  File written to " + OUTPUT_FILE_NAME);
        try {
            writer.close();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        try {
            file.close();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        // System.out.println("\nFinished.  File written to " + file);

        logger.info(orders.size() + " orders generated.");
    }
}
