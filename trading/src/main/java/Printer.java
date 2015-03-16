import java.io.FileWriter;
import java.util.ArrayList;

/**
 * This class is responsible for producing the output file.
 */
public class Printer {

    /**
     * Appends orders to a csv file.
     * @param orders An ArrayList of Orders.
     * @param file The file to write to.
     */
    public static void printOrders(ArrayList<Order> orders, FileWriter file){
        CSVWriter writer = new CSVWriter(f, '\t');
        for(Order o : orders){
            writer.writeAll(o.toStringArray());
        }
        System.out.println("\nFinished.  File written to " + file);

    }
}
