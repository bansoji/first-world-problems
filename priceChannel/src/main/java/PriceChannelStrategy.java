import date.DateUtils;
import main.OrderType;
import quickDate.Order;
import quickDate.Price;
import utils.Point;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * An implementation of the Price Channel Strategy.
 */
public class PriceChannelStrategy implements TradingStrategy {
    private List<Price> prices;
    private List<Order> ordersGenerated;
    private int minWindowSize;
    private double variance;
    private double threshold;
    private int volume;
    private String startDate;
    private String endDate;

    private static final Logger logger = Logger.getLogger("log");

    public PriceChannelStrategy(List<Price> historicalPrices, InputStream config) {
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
                "Minimum Window Size: " + this.minWindowSize + "\n" +
                "Variance: " + this.variance + "\n" +
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
     *
     * @param prop a Properties object, containing the configuration parameters of
     *             the strategy module.
     */
    private void configureStrategy(Properties prop) {
        // Configure the strategy using parameters config properties file.
        // Defaults are the same as in MSM spec.
        this.minWindowSize = Integer.parseInt(prop.getProperty("minWindowSize", "4"));
        this.variance = Double.parseDouble(prop.getProperty("variance", "4"));
        this.threshold = Double.parseDouble(prop.getProperty("threshold", "0.001"));
        this.volume = Integer.parseInt(prop.getProperty("volume", "100"));

        startDate = prop.getProperty("startDate");
        endDate = prop.getProperty("endDate");
    }

    @Override
    public void generateOrders() {
        // This will trigger the pipeline to generate orders.
        List<Point> priceInput = new ArrayList<>();
        for (Price p : prices) {
            priceInput.add(new Point((double)DateUtils.parse(p.getDate()).getMillis(), p.getValue()));       // TODO: This could potentially be optimised.
        }

        //result.get(0) returns lows. result.get(1) returns highs.
        List<List<Point>> result = calculateLowsAndHighs(priceInput, minWindowSize, variance);
        List<Point> lows = result.get(0);
        List<Point> highs = result.get(1);
        //TODO: Rest of method needs to be completed after Channel class is able to return 2 lines.
        //Channel channel = new Channel(lows, highs);
        //double lowGradient =
        //double lowB =
        //double highGradient =
        //double highB =


    }

    @Override
    public List<Order> getOrders() {
        return ordersGenerated;
    }

    public List<List<Point>> calculateLowsAndHighs(List<Point> priceInput, int minWindowSize, double variance) {
        List<List<Point>> result = new ArrayList<>();
        ArrayList<Point> highs = new ArrayList<>();
        ArrayList<Point> lows = new ArrayList<>();

        int j;
        for (int i = 0; i < priceInput.size(); i += j) {
            Point min = new Point (0.00, Double.MAX_VALUE);
            Point max = new Point (0.00, Double.MIN_VALUE);

            boolean highsAndLowsAdded = false;
            for (j = 0; i + j < priceInput.size() && !highsAndLowsAdded; j++) {
                Point price = priceInput.get(j + i);
                if (price.getY() < min.getY()) {
                    min = price;
                } else if (price.getY() > max.getY()) {
                    max = price;
                }

                if (j >= minWindowSize && max.getY() - min.getY() >= variance) {
                    highs.add(max);
                    lows.add(min);
                    highsAndLowsAdded = true;
                }
            }
        }
        result.add(lows);
        result.add(highs);
        return result;
    }

    //private List<OrderType> generateTradeSignals(List<Double priceInput, Line high, Line low, double threshold){
    //List<OrderType> l = new ArrayList<OrderType>();
    //  for (int i=1; i<priceInput.size(); i++){
    //  if point > highLine and lastMove == SELL, buy
    //  if point < lowLine and lastMove == BUY, sell
    //  }
    //  return l;
    //}
}
