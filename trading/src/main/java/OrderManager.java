import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Main class.
 */

public class OrderManager {

    // Some class constants.
    public static final String VERSION = "0.6.0";

    public static void main(String[] args) throws IOException {
        String fileName = args[0];
        String paramName = args[1]; // To use, go to "Run -> Edit Configurations" and add
        // "common/src/main/resources/sampleData trading/resources/config.properties" to program args

        ///////////////////// Initialisation. ///////////////////////////////
        // Logger initialisation.
        Logger logger = Logger.getLogger("log");
        FileHandler handler = new FileHandler("logfile.log");
        SimpleFormatter formatter = new SimpleFormatter();
        handler.setFormatter(formatter);
        logger.addHandler(handler);

        logger.info("====== Project Name =========\n" +
                "Developer Team: Group 1\n" +
                "MODULE VERSION: " + VERSION + "\n" +
                "INPUT FILE: " + fileName);

        // Load the csv file.
        TransactionReader tReader = new TransactionReader(fileName);
        List<Price> allPrices = tReader.getAllPrices();

        // Initialise the trading strategy.
        TradingStrategy strategy = new MomentumStrategy(allPrices);

        // Initialise and Load the properties file.
        Properties prop = new Properties();
        InputStream input = null;
        input = new FileInputStream(paramName);
        prop.load(input);

        // Configure the strategy.
        String movingAvg = prop.getProperty("movingAverage", "4"); // Sets tge default values.
        String threshold = prop.getProperty("threshold", "0.001");
        String volume = prop.getProperty("volume", "100");

        strategy.setMovingAverage(Integer.parseInt(movingAvg));
        strategy.setThreshold(Double.parseDouble(threshold));
        strategy.setVolume(Integer.parseInt(volume));

        // Initialise the timer.
        long startTime = System.currentTimeMillis();

        ///////////////////////////// Running. /////////////////////////////

        // Run the strategy module.
        strategy.generateOrders();
        List<Order> ordersGenerated = strategy.getOrders();

        FileWriter file = new FileWriter("output.csv");
        Printer.printOrders(ordersGenerated, file);
        file.close();

        ///////////////////////////// Post running. /////////////////////////
        // Stop the timer.
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Time passed = " + elapsedTime + "ms");
        logger.info("Time Elapsed : " + elapsedTime + "ms");

        // Log successful.
        logger.info("Module successful. No errors encountered.");
    }

    private static void printProfitability(List<Order> ordersGenerated){
        double profit = 0.0;
        for (Order oo : ordersGenerated){
            System.out.println(oo.totalTransactionValue());
            profit += oo.totalTransactionValue();
        }
        System.out.println("Profitability is " + profit);
    }

    private static void printPrices(List<Price> allPrices){
        //prints allPrices

        for (Price price: allPrices){
            System.out.print(price.getCompanyName() + " ");
            System.out.print(price.getValue() + " ");
            System.out.print(price.getDate() + "\n");
        }


        //prints columnContents
        /*
        for (String line:columnContents){
            System.out.println(line);
        }
        */
    }
}
