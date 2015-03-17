import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//test

/**
 * This class is responsible for producing the output file.
 */
public class Printer {

    /**
     * Appends orders to a csv file.
     * @param orders An ArrayList of Orders.
     * @param file The file to write to.
     */
    public static void printOrders(ArrayList<Order> orders, FileWriter file) throws IOException {
        CSVWriter writer = new CSVWriter(file, '\t');
        for(Order o : orders){
            System.out.print("Order");
            writer.writeNext(o.toStringArray());
        }
        System.out.println("\nFinished.  File written to " + file);

    }
}
