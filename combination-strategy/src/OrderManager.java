import date.DateUtils;
import quickDate.*;
import main.Reader;
import utils.GeometryUtils;
import utils.Line;
import utils.Point;

import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Main class.
 */

public class OrderManager {

    // Some class constants.
    public static final String VERSION = "1.1.0";

    public static void main(String[] args) throws IOException {
        int combinationWindow = 80;

        long startTime = System.currentTimeMillis();
        if (args.length != 2){
            System.err.println("Error: Incorrect program usage.");
            System.err.println("Usage: java -jar <BuyHardModule> <pricesFile> <paramFile>");
            return;
        }

        String fileName = args[0];
        String paramName = args[1]; // To use, go to "Run -> Edit Configurations" and add
        // "common/src/main/resources/sampleData trading/resources/config.properties" to program args

        ///////////////////////////////
        // INITIALISATION.
        ///////////////////////////////
        // Logger initialisation.
        Logger logger = Logger.getLogger(FileManager.LOG_NAME);
        logger.setUseParentHandlers(false);

        // Load the properties file.
        InputStream config = new FileInputStream(paramName);
        Properties prop = new Properties();

        try {
            prop.load(config);
        } catch (IOException e) {
            logger.severe("Invalid Parameters File.");
            e.printStackTrace();
        }
        config.close();

        String outputFileName = prop.getProperty("outputFileName", FileManager.OUTPUT_FILE);
        String logFileName = prop.getProperty("outputLogName", FileManager.LOG_FILE);

        FileHandler handler = new FileHandler(logFileName);
        SimpleFormatter formatter = new SimpleFormatter();
        handler.setFormatter(formatter);
        logger.addHandler(handler);

        logger.info("====== Buy Hard =========\n" +
                "Developer Team: Group 1\n" +
                "MODULE NAME: BuyHard-Combination-" + VERSION + ".jar\n" +
                "MODULE VERSION: " + VERSION + "\n" +
                "INPUT FILE: " + fileName + "\n" +
                "OUTPUT FILE: " + outputFileName + "\n" +
                "LOG FILE: " + logFileName);

        // Load the csv file.
        Reader tReader = new PriceReader(fileName);
        tReader.readAll();

        // Initialise the File and CSV Writer.
        BufferedWriter orderFile;
        try {
            orderFile = new BufferedWriter(new FileWriter(outputFileName));
        } catch (IOException e) {
            logger.severe(e.getMessage());
            return;
        }

        OrderWriter csvOrderWriter = new OrderWriter(orderFile);
        //endTime = System.currentTimeMillis();
        // Initialise the timer.
        //startTime = System.currentTimeMillis();

        //Create a ParameterManager for the strategy constructor.
        ParameterManager pManager = new ParameterManager<Number>();
        pManager.updateParams(paramName);
        Properties props = pManager.getProperties(paramName);
        Enumeration properties = props.propertyNames();

        while (properties.hasMoreElements()){
            String key = (String)properties.nextElement();
            //if the value of the property is not numerical, it is not a parameter
            String value = props.getProperty(key);
            if (!format.FormatChecker.isDouble(value)) continue;
            boolean isInteger = FormatChecker.isInteger(value);
            if (isInteger) {
                pManager.put(key, value);
            }
        }

        for (String company: (Set<String>)tReader.getHistory().getAllCompanies()) {
            logger.info("Analysing prices for " + company);
            List<Price> companyHistory = tReader.getCompanyHistory(company);
            // PrintUtils.printPrices(companyHistory);

            // Initialise the trading strategy.
            TradingStrategy strategy = new CombinationStrategy(companyHistory, pManager, paramName);

            ///////////////////////////////
            // RUNNING.
            ///////////////////////////////

            // Run the strategy module.
            strategy.generateOrders();
            List<Order> ordersGenerated = strategy.getOrders();
            csvOrderWriter.writeOrders(ordersGenerated);
        }

        ///////////////////////////////
        // FINISHING.
        ///////////////////////////////

        // Stop the timer.
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        // System.out.println("Time passed = " + elapsedTime + "ms");
        logger.info("Time Elapsed : " + elapsedTime + "ms");

        //startTime = System.currentTimeMillis();
        // Close the orders file and CSV Writer.
        csvOrderWriter.closeWriter();
        try {
            orderFile.close();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }

        handler.close();
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
