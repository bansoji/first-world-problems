import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Main class
 */

public class OrderManager {

    // Some class constants.
    public static final String VERSION = "0.4.0";

    public static void main(String[] args) throws IOException {
        String fileName = args[0];

        TransactionReader tReader = new TransactionReader(fileName);
        ArrayList<Price> allPrices = tReader.getAllPrices();

        TradingStrategy strategy = new MomentumStrategy(allPrices);
        strategy.generateOrders();
        ArrayList<Order> ordersGenerated = strategy.getOrders();
        // Printer.printOrders(ordersGenerated, new FileWriter("output.csv"));
        // ArrayList<String> columnContents = tReader.getColumnContents(1);

        double profit = 0.0;
        for (Order oo : ordersGenerated){
            System.out.println(oo.getValue());
            profit += oo.getValue();
        }
        System.out.println("Profitability is " + profit);


        /*
         * Under construction - Edwin
         */
        Logger logger = Logger.getLogger("log");
        FileHandler handler = new FileHandler("text.log");
        SimpleFormatter formatter = new SimpleFormatter();
        handler.setFormatter(formatter);
        logger.addHandler(handler);

        logger.info("Testing 1st message");

        //prints allPrices
        /*
        for (Price price: allPrices){
            System.out.print(price.getCompanyName() + " ");
            System.out.print(price.getValue() + " ");
            System.out.print(price.getDate() + "\n");
        }
        */

        //prints columnContents
        /*
        for (String line:columnContents){
            System.out.println(line);
        }
        */
    }

}
