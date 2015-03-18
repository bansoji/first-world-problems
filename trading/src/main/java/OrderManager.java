import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Main class
 */

public class OrderManager {

    // Some class constants.
    public static final String VERSION = "0.4.0";

    public static void main(String[] args) throws IOException {
        String fileName = args[0];
        String paramName = args[1];

        // Load the csv file.
        TransactionReader tReader = new TransactionReader(fileName);
        ArrayList<Price> allPrices = tReader.getAllPrices();

        TradingStrategy strategy = new MomentumStrategy(allPrices);

        // Load the properties file.
        Properties prop = new Properties();
        InputStream input = null;
        input = new FileInputStream(paramName);
        prop.load(input);

        // Configure the strategy.
        String movingAvg = prop.getProperty("movingAverage", "4");
        String threshold = prop.getProperty("threshold", "0.001");
        String volume = prop.getProperty("volume", "100");

        strategy.setMovingAverage(Integer.parseInt(movingAvg));
        strategy.setThreshold(Double.parseDouble(threshold));
        strategy.setVolume(Integer.parseInt(volume));

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
        System.out.println(movingAvg);

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
