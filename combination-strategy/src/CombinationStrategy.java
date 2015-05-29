import date.DateUtils;
import utils.FinanceUtils;
import quickDate.Order;
import main.OrderType;
import quickDate.Price;
import utils.GeometryUtils;
import utils.Line;
import utils.Point;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
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
    ParameterManager<Number> config;
    String configFileName;

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

    public CombinationStrategy(List<Price> historicalPrices, ParameterManager<Number> config, String configFileName) {
        this.prices = historicalPrices;
        this.ordersGenerated = new ArrayList<Order>();
        this.config = config;
        this.configFileName = configFileName;

        // Initialise the config according to the parameters.
        configureStrategy(config.getProperties(configFileName));

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

        startDate = prop.getProperty("startDate");
        endDate = prop.getProperty("endDate");
    }

    @Override
    public void generateOrders() {
        int i = 0;
        List<Price> strategyInput;
        TradingStrategy strategy;

        while (i < prices.size()) {
            if (i + combinationWindow < prices.size()) {
                strategyInput = prices.subList(i, i + combinationWindow);
            } else {
                strategyInput = prices.subList(i, prices.size());
            }

            //Make prices into points
            List<Point> priceInput = new ArrayList<>();
            for (Price p : strategyInput) {
                priceInput.add(new Point((double) DateUtils.parseMonthAbbr(p.getDate()).getMillis(), p.getValue()));       // TODO: This could potentially be optimised.
            }
            Line line = GeometryUtils.createLine(priceInput);

            if (line.getSlope() < -1) {
                strategy = new NullStrategy(strategyInput, config, configFileName);
            } else if (line.getSlope() < 0) {
                strategy = new BuyHard(strategyInput, config, configFileName);
            } else if (line.getSlope() < 1) {
                strategy = new BuyHardVengeance(strategyInput, config, configFileName);
            } else {
                strategy = new PriceChannelStrategy(strategyInput, config, configFileName);
            }
            strategy.generateOrders();
            List<Order> subOrdersGenerated = strategy.getOrders();
            ordersGenerated.addAll(subOrdersGenerated);

            i += combinationWindow;
        }
    }

    @Override
    public List<Order> getOrders() {
        return ordersGenerated;
    }
}