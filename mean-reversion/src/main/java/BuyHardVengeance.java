import date.DateUtils;
import main.OrderType;
import quickDate.Price;
import quickDate.Order;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * An implementation of the Mean Reversion Strategy.
 */
public class BuyHardVengeance implements TradingStrategy {
    private List<Price> prices;
    private List<Order> ordersGenerated;
    private int volume;
    private String startDate;
    private String endDate;
    private double mean;
    private double threshold;

    private static final Logger logger = Logger.getLogger("log");

    public BuyHardVengeance(List<Price> historicalPrices, InputStream config) {
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

        String parameters = "Parameters Used:\n" + "\n" +
                "Threshold: " + this.threshold + "\n" +
                "Volume: " + this.volume;

        if (startDate != null) {
            parameters += "\nStart Date: " + this.startDate;
        }
        if (endDate != null) {
            parameters += "\nEnd Date: " + this.endDate;
        }
        logger.info(parameters);
    }

    /**
     * Configure the strategy given a Properties file.
     * @param prop a Properties object, containing the configuration parameters of
     *             the strategy module.
     */
    private void configureStrategy(Properties prop) {
        this.threshold = Double.parseDouble(prop.getProperty("mReversionThreshold", "0.001"));
        this.volume = Integer.parseInt(prop.getProperty("mReversionVolume", "100"));

        startDate = prop.getProperty("startDate");
        endDate = prop.getProperty("endDate");
    }

    @Override
    public void generateOrders() {
        this.mean = 0;
        OrderType nextStatus = OrderType.BUY; // The next status to look for.

        for (Price p : prices){
            // Update the mean
            mean += p.getValue();
            mean = mean/2.0;

            //Check if orders need to be generated for today.
            if (startDate != null && DateUtils.before(p.getDate(), startDate)) continue;
            if (endDate != null && DateUtils.after(p.getDate(), endDate)) break;

            // If the price is lower, issue a buy.
            if (nextStatus == OrderType.BUY){
                if (p.getValue() < mean - (mean*threshold)) {
                    // Create the order.
                    Order o = new Order(nextStatus, p.getCompanyName(), p.getValue(), volume, p.getDate());
                    ordersGenerated.add(o);
                    nextStatus = nextStatus.getOppositeOrderType();
                }
            }


            // If the price is above, issue a sell.
            if (nextStatus == OrderType.SELL){
                if (p.getValue() > mean + (mean*threshold)) {
                    // Create the order.
                    Order o = new Order(nextStatus, p.getCompanyName(), p.getValue(), volume, p.getDate());
                    ordersGenerated.add(o);
                    nextStatus = nextStatus.getOppositeOrderType();
                }
            }


        }
    }

    @Override
    public List<Order> getOrders() {
        return ordersGenerated;
    }
}
