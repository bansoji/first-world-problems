import date.DateUtils;
import core.OrderType;
import quickDate.Order;
import quickDate.Price;
import utils.Channel;
import utils.Line;
import utils.Point;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Double;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static utils.GeometryUtils.predictedPrice;

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

    public PriceChannelStrategy(List<Price> historicalPrices, Properties prop) {
        this.prices = historicalPrices;
        this.ordersGenerated = new ArrayList<Order>();

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
            priceInput.add(new Point((double)DateUtils.parseMonthAbbr(p.getDate()).getMillis(), p.getValue()));       // TODO: This could potentially be optimised.
        }

        // result.get(0) returns low points.
        // result.get(1) returns high points.gg
        List<List<Point>> result = calculateLowsAndHighs(priceInput, minWindowSize, variance); //Need to handle case where method does not return enough values.
        Channel channel = new Channel(result.get(0), result.get(1));
        Line lowLine = channel.getLowLine();    //Returns a line of best fit given low points.
        Line highLine = channel.getHighLine();  //Returns a line of best fit given high points.

        // Calculate trade signals.
        List<OrderType> tradeSignals = generateTradeSignals(priceInput, lowLine, highLine, threshold);

        // Generate orders.
        OrderType nextStatus = OrderType.NOTHING; // The next status to look for.

        for (int i=0; i<tradeSignals.size(); i++) {
            if (ordersGenerated.size() == 0){
                // Starting case.
                if (tradeSignals.get(i).equals(OrderType.NOTHING))
                    continue;

                // Skip if the date given is out of the simulation date range.
                Price tradePrice = prices.get(i);

                if (startDate != null && DateUtils.before(tradePrice.getDate(), startDate)) continue;
                if (endDate != null && DateUtils.after(tradePrice.getDate(), endDate)) continue;

                // Create a new Order.
                OrderType currentSignal = tradeSignals.get(i);
                Order o = new Order(currentSignal, tradePrice.getCompanyName(), tradePrice.getValue(),
                        volume, tradePrice.getDate());
                ordersGenerated.add(o);

                // Toggle the nextStatus.
                nextStatus = currentSignal.getOppositeOrderType();
            }

            if (tradeSignals.get(i).equals(nextStatus)) {
                // Create an order using this ith day.
                // Get the price for that day.
                Price tradePrice = prices.get(i);

                // Skip if the date given is out of the simulation date range.
                if (startDate != null && DateUtils.before(tradePrice.getDate(), startDate)) continue;
                if (endDate != null && DateUtils.after(tradePrice.getDate(), endDate)) continue;

                // Create a new Order.
                Order o = new Order(nextStatus, tradePrice.getCompanyName(), tradePrice.getValue(),
                        volume, tradePrice.getDate());
                ordersGenerated.add(o);

                // Toggle the nextStatus.
                nextStatus = nextStatus.getOppositeOrderType();
            }
        }

    }

    @Override
    public List<Order> getOrders() {
        return ordersGenerated;
    }

    private List<List<Point>> calculateLowsAndHighs(List<Point> priceInput, int minWindowSize, double variance) {
        List<List<Point>> result = new ArrayList<>();
        ArrayList<Point> highs = new ArrayList<>();
        ArrayList<Point> lows = new ArrayList<>();

        int j;
        for (int i=0; i < priceInput.size(); i+=j) {
            Point min = new Point (-1, Double.MAX_VALUE);
            Point max = new Point (-1, Double.MIN_VALUE);

            boolean highsAndLowsAdded = false;
            for (j=0; i+j < priceInput.size() && !highsAndLowsAdded; j++) {
                Point price = priceInput.get(i+j);
                if (price.getY() < min.getY()) {
                    min = price;
                } else if (price.getY() > max.getY()) {
                    max = price;
                }

                if (j >= minWindowSize && max.getY()-min.getY() >= variance) {
                    lows.add(min);
                    highs.add(max);
                    highsAndLowsAdded = true;
                }
            }
        }
        result.add(lows);
        result.add(highs);
        return result;
    }

    private List<OrderType> generateTradeSignals(List<Point> priceInput, Line low, Line high, double threshold){
        List<OrderType> l = new ArrayList<OrderType>();
        l.add(OrderType.NOTHING); // First day is always NOTHING.

        for (int i=1; i<priceInput.size(); i++){
            double pDate = priceInput.get(i).getX();
            double pPrice = priceInput.get(i).getY();
            double predictedLowPrice = predictedPrice(low, pDate);
            double predictedHighPrice = predictedPrice(high, pDate);

            if (pPrice >= predictedHighPrice-threshold) {
                l.add(OrderType.SELL);
            } else if (pPrice <= predictedLowPrice+threshold) {
                l.add(OrderType.BUY);
            } else {
                l.add(OrderType.NOTHING);
            }
        }
        return l;
    }
}
