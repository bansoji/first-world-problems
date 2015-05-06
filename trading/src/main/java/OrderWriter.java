import com.opencsv.CSVWriter;
import quickDate.Order;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for producing the output file.
 */
public class OrderWriter {

    private static final Logger logger = Logger.getLogger(FileManager.LOG_NAME);
    private static final String OUTPUT_FILE_NAME = FileManager.OUTPUT_FILE;
    private CSVWriter writer;
    private int numOrdersGenerated;

    public OrderWriter(BufferedWriter file){
        this.writer = new CSVWriter(file, ',', CSVWriter.NO_QUOTE_CHARACTER);
        String[] header = new String[] {"#RIC", "Date", "Price", "Volume", "Value", "Signal"};
        // Print the header.
        writer.writeNext(header);

        this.numOrdersGenerated = 0;
    }

    public void closeWriter(){
        logger.log(Level.INFO, "Finished. File written to " + OUTPUT_FILE_NAME);

        // System.out.println("\nFinished.  File written to " + file);
        logger.info(this.numOrdersGenerated + " orders generated.");

        try {
            writer.close();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }

    }

    /**
     * Appends orders to a csv file.
     * @param orders An ArrayList of Orders.
     */
    public void writeOrders(List<Order> orders) {

        for (Order o : orders){
            this.writer.writeNext(o.toStringArray());
            this.numOrdersGenerated += 1;
        }

    }
}
