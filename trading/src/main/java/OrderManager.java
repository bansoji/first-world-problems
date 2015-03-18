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

        FileWriter file = new FileWriter("output.csv");
        Printer.printOrders(ordersGenerated, file);
        file.close();
        // ArrayList<String> columnContents = tReader.getColumnContents(1);

        double profit = 0.0;
        for (Order oo : ordersGenerated){
            System.out.println(oo.totalTransactionValue());
            profit += oo.totalTransactionValue();
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

        logger.info("Developer Team: Group 1(?)");
        logger.info("MODULE VERSION: " + VERSION);
        logger.info("INPUT FILE: " + args[0]);

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
