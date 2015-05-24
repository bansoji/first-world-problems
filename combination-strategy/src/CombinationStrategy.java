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
 * An implementation of the Combination Strategy.
 */
public class CombinationStrategy implements TradingStrategy {
    private List<Price> prices;
    private List<Order> ordersGenerated;

    private int combinationWindow;

    private int momentumMovingAvgTimeWindow;
    private double momentumThreshold;
    private int momentumVolume;

    private double mReversionThreshold;
    private int mReversionVolume;

    private int channelMinWindowSize;
    private int channelSize;
    private double channelVariance;
    private double channelThreshold;
    private int channelVolume;




    private String startDate;
    private String endDate;

    private static final Logger logger = Logger.getLogger("log");

    public CombinationStrategy(List<Price> historicalPrices, InputStream config) {
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
                "Combination Window: " + this.combinationWindow + "\n" +
                
                "Momentum Moving Average Time Window: " + this.momentumMovingAvgTimeWindow + "\n" +
                "Momentum Threshold: " + this.momentumThreshold + "\n" +
                "Momentum Volume: " + this.momentumVolume + "\n\n" +

                "Mean Reversion Threshold: " + this.mReversionThreshold + "\n" +
                "Mean Reversion Volume: " + this.mReversionVolume + "\n" +

                "Channel Min Window Size: " + this.channelMinWindowSize + "\n" +
                "Channel Size: " + this.channelSize + "\n" +
                "Channel Variance: " + this.channelVariance + "\n" +
                "Channel Threshold: " + this.channelThreshold + "\n" +
                "Channel Volume: " + this.channelVolume + "\n";

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
        // Configure the strategy using parameters config properties file.
        // Defaults are the same as in MSM spec.

        this.combinationWindow = Integer.parseInt(prop.getProperty("combinationWindow", "50"));

        // Momentum strategy parameters.
        this.momentumMovingAvgTimeWindow = Integer.parseInt(prop.getProperty("momentumMovingAvgTimeWindow", "4"));
        this.momentumThreshold = Double.parseDouble(prop.getProperty("momentumThreshold", "0.001"));
        this.momentumVolume = Integer.parseInt(prop.getProperty("momentumVolume", "100"));

        // Mean Reversion strategy parameters.
        this.mReversionThreshold = Double.parseDouble(prop.getProperty("mReversionThreshold", "0.001"));
        this.mReversionVolume = Integer.parseInt(prop.getProperty("mReversionVolume", "100"));

        // Price Channel strategy parameters.
        this.channelMinWindowSize = Integer.parseInt(prop.getProperty("channelMinWindowSize", "2"));
        this.channelSize = Integer.parseInt(prop.getProperty("channelSize", "50"));
        this.channelVariance = Double.parseDouble(prop.getProperty("variance", "0.001"));
        this.channelThreshold = Double.parseDouble(prop.getProperty("threshold", "0.00"));
        this.channelVolume = Integer.parseInt(prop.getProperty("volume", "100"));

        // Null strategy parameters.

        startDate = prop.getProperty("startDate");
        endDate = prop.getProperty("endDate");
    }

    @Override
    public void generateOrders() {
        //grab the first combinationWindow number of prices, put into a cut arrayList.
        //use linear regression, find line of best fit
        //if gradient < something, then use null strat
        //if gradient = something, then use strategyA
            //if class has not been constructed, construct it, giving it sub-arraylist + config? (this might not work)..
            //if class has been constructed, Strategy.setPrices(subarraylist)
            //strategy.generateOrders()
            //strategy.getOrders() and add it to Combination's strategy order list.
        //if gradient = something, then use strategyB

        //if gradient > something, then use strategyC




    }

    @Override
    public List<Order> getOrders() {
        return ordersGenerated;
    }
}