import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Main class.
 */

public class OrderManager {

    // Some class constants.
    public static final String VERSION = "0.9.0";
    public static final String OUTPUT_FILE = "orders.csv";
    public static final String LOG_FILE = "logfile.log";

    public static void main(String[] args) throws IOException {
        if (args.length != 2){
            System.out.println("Error: Incorrect program usage.");
            System.out.println("Usage: java -jar BuyHard inputFile paramsFile");
            return;
        }
        String fileName = args[0];
        String paramName = args[1]; // To use, go to "Run -> Edit Configurations" and add
        // "common/src/main/resources/sampleData trading/resources/config.properties" to program args

        ///////////////////////////////
        // INITIALISATION.
        ///////////////////////////////

        // Logger initialisation.
        Logger logger = Logger.getLogger("log");
        FileHandler handler = new FileHandler(LOG_FILE);
        SimpleFormatter formatter = new SimpleFormatter();
        handler.setFormatter(formatter);
        logger.addHandler(handler);

        logger.info("====== Buy Hard =========\n" +
                "Developer Team: Group 1\n" +
                "MODULE VERSION: " + VERSION + "\n" +
                "INPUT FILE: " + fileName + "\n" +
                "OUTPUT FILE: " + OUTPUT_FILE + "\n" +
                "LOG FILE: " + LOG_FILE);

        // Load the csv file.
        Reader tReader = new PriceReader(fileName);
        tReader.readAll();


        List<Price> companyHistory = tReader.getCompanyHistory("BHP.AX");
        // PrintUtils.printPrices(companyHistory);

        // Load the properties file.
        InputStream input = new FileInputStream(paramName);

        // Initialise the trading strategy.
        TradingStrategy strategy = new MomentumStrategy(companyHistory,input);

        // Initialise the timer.
        long startTime = System.currentTimeMillis();


        ///////////////////////////////
        // RUNNING.
        ///////////////////////////////

        // Run the strategy module.
        strategy.generateOrders();
        List<Order> ordersGenerated = strategy.getOrders();

        Printer.printOrders(ordersGenerated);


        ///////////////////////////////
        // FINISHING.
        ///////////////////////////////

        // Stop the timer.
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        // System.out.println("Time passed = " + elapsedTime + "ms");
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
        //prints allPrices.
        for (Price price: allPrices){
            System.out.print(price.getCompanyName() + " ");
            System.out.print(price.getValue() + " ");
            System.out.print(price.getDate() + "\n");
        }
    }
}
