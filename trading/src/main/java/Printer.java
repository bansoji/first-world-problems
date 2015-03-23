import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is responsible for producing the output file.
 */
public class Printer {

    private static final Logger logger = Logger.getLogger("log");

    /**
     * Appends orders to a csv file.
     * @param orders An ArrayList of Orders.
     * @param file The file to write to.
     */
    public static void printOrders(List<Order> orders, FileWriter file) {
        CSVWriter writer = new CSVWriter(file, ',');
        for(Order o : orders){
            //System.out.println(Arrays.toString(o.toStringArray())); // For debugging.
            writer.writeNext(o.toStringArray());
        }
        System.out.println("\nFinished.  File written to " + file);

    }
}
