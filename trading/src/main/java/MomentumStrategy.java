import finance.FinanceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * An implementation of the Momentum Strategy.
 */
public class MomentumStrategy implements TradingStrategy {

    private List<Price> prices;
    private List<Order> ordersGenerated;
    private int movingAverage;
    private double threshold;
    private int volume;
    private Date startDate;
    private Date endDate;


    private static final Logger logger = Logger.getLogger("log");

    public MomentumStrategy(List<Price> historicalPrices, InputStream config) {
        this.prices = historicalPrices;
        this.ordersGenerated = new ArrayList<Order>();

        // Initialise the config according to the parameters.
        Properties prop = new Properties();
        try {
            prop.load(config);
        } catch (IOException e) {
            logger.severe("Invalid Parameters File.");
            e.printStackTrace();
        }

        configureStrategy(prop);

        String parameters = "Parameters Used:\n" +
                "Moving Average: " + this.movingAverage + "\n" +
                "Threshold: " + this.threshold + "\n" +
                "Volume: " + this.volume;

        if (startDate != null){
            parameters = parameters + "\nStart Date: " + this.startDate;
        }
        if (endDate != null){
            parameters = parameters + "\nEnd Date: " + this.endDate;
        }

        logger.info(parameters);
    }

    /**
     * Configure the strategy given a Properties file.
     * @param prop a Properties object, containing the configuration parameters of
     *             the strategy module.
     */
    private void configureStrategy(Properties prop) {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        // Configure the strategy using parameters config properties file.
        // Defaults are the same as in MSM spec.
        this.movingAverage = Integer.parseInt(prop.getProperty("movingAverage", "4"));
        this.threshold = Double.parseDouble(prop.getProperty("threshold", "0.001"));
        this.volume = Integer.parseInt(prop.getProperty("volume", "100"));

        // Get the start and end dates.
        try {
            this.startDate = df.parse(prop.getProperty("startDate"));
        } catch (ParseException e) {
            logger.warning("Incorrect Date format used for Start Date of simulations. " +
                    "Please make sure it is in the correct format of dd-MM-yyyy.");
            startDate = null;
        } catch (NullPointerException e){
            startDate = null;
        }
        try {
            this.endDate = df.parse(prop.getProperty("endDate"));
        } catch (ParseException e) {
            logger.warning("Incorrect Date format used for End Date of simulations. " +
                    "Please make sure it is in the correct format of dd-MM-yyyy.");
            endDate = null;
        }catch (NullPointerException e){
            endDate = null;
        }
    }

    /**
     * Generate orders using this strategy.
     */
    @Override
    public void generateOrders() {
        // This will trigger the pipeline to generate orders.
        List<Double> priceInput = new ArrayList<Double>();
        for (Price p : prices){
            priceInput.add(p.getValue());       // TODO: This could potentially be optimised.
            //System.out.println(p.getValue());
        }

        List<Double> sma = FinanceUtils.calcAllSimpleMovingAvg(priceInput, movingAverage);

        // Calculate Trade Signals.
        List<OrderType> tradeSignals = generateTradeSignals(sma, threshold);

        /*
        for (Double d : sma){
            System.out.println(d);
        }

        for (OrderType s : tradeSignals){
            System.out.println(s);
        }
        */

        // Generate the orders.
        OrderType nextStatus = OrderType.BUY; // The next status to look for.
        for (int i=0; i<tradeSignals.size(); i++){
            if (tradeSignals.get(i).equals(nextStatus)){
                // Create an order using this ith day.
                // Get the price for that day. Offset by moving average.
                Price tradePrice = prices.get(i + movingAverage -1);

                // Skip if the date given is out of the simulation date range.
                if (startDate != null && tradePrice.getDate().before(startDate)) continue;
                if (endDate != null && tradePrice.getDate().after(endDate)) continue;

                // Create a new Order.
                Order o = new Order(nextStatus, tradePrice.getCompanyName(), tradePrice.getValue(),
                        volume, tradePrice.getDate());
                //System.out.println("Out");
                ordersGenerated.add(o);

                // Toggle the nextStatus.
                nextStatus = nextStatus.getOppositeOrderType();
            }
        }
    }

    /**
     * Gets all the orders that have been generated using this strategy.
     * @return an ArrayList of Orders.
     */
    @Override
    public List<Order> getOrders() {
        return ordersGenerated;
    }

    /**
     * Generate the trade signals.
     * @param sma A List of Doubles containing the simple moving average.
     * @param threshold A threshold to filter when to create a buy or sell signal.
     * @return a List containing OrderTypes.
     */
    private List<OrderType> generateTradeSignals(List<Double> sma, double threshold){

        List<OrderType> l = new ArrayList<OrderType>();
        l.add(OrderType.NOTHING); // First day is always NOTHING.

        for (int i=1; i<sma.size(); i++){
            Double difference = sma.get(i) - sma.get(i-1);
            if (difference > threshold){
                l.add(OrderType.BUY);
            } else if (difference < (threshold * -1)) {
                l.add(OrderType.SELL);
            } else {
                l.add(OrderType.NOTHING);
            }
        }

        return l;
    }

    public void setMovingAverage(int movingAverage) {
        this.movingAverage = movingAverage;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }


}
