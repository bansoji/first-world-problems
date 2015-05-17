import date.DateUtils;
import utils.FinanceUtils;
import quickDate.Order;
import main.OrderType;
import quickDate.Price;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Live Free or Buy Hard.
 * Doesn't sound very wise, so placing no orders is much better here.
 * Use in adverserial market conditions. 
 */
public class NullStrategy implements TradingStrategy {

    private static final Logger logger = Logger.getLogger("log");

    public NullStrategy(List<Price> historicalPrices, InputStream config) {
       
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
                "Moving Average Time Window: " + this.movingAvgTimeWindow + "\n" +
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
		return; 
    }

    /**
     * Generate orders using this strategy.
     */
    @Override
    public void generateOrders() {
        return;
    }

    /**
     * Gets all the orders that have been generated using this strategy.
     * @return an ArrayList of Orders.
     */
    @Override
    public List<Order> getOrders() {
        return new List<Order>();
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

    public void setMovingAvgTimeWindow(int movingAvgTimeWindow) {
        this.movingAvgTimeWindow = movingAvgTimeWindow;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }


}
