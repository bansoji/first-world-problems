import date.DateUtils;
import core.OrderType;
import quickDate.Order;
import quickDate.Price;
import utils.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Double;
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
    private List<OrderType> tradeSignals;
    private int minWindowSize;
    private int channelSize;
    private double variance;
    private double threshold;
    private int volume;
    private String startDate;
    private String endDate;

    private static final Logger logger = Logger.getLogger("log");

    public PriceChannelStrategy(List<Price> historicalPrices, Properties prop) {
        this.prices = historicalPrices;
        this.ordersGenerated = new ArrayList<Order>();
        this.tradeSignals = new ArrayList<OrderType>();

        configureStrategy(prop);

        String parameters = "Parameters Used:\n" +
                "Minimum Window Size: " + this.minWindowSize + "\n" +
                "Channel Size: " + this.channelSize + "\n" +
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
        this.minWindowSize = Integer.parseInt(prop.getProperty("minWindowSize", "2"));
        this.channelSize = Integer.parseInt(prop.getProperty("channelSize", "50"));
        this.variance = Double.parseDouble(prop.getProperty("variance", "0.001"));
        this.threshold = Double.parseDouble(prop.getProperty("threshold", "0.00"));
        this.volume = Integer.parseInt(prop.getProperty("volume", "100"));

        this.startDate = prop.getProperty("startDate");
        this.endDate = prop.getProperty("endDate");
    }

    @Override
    public void generateOrders() {
        // This will trigger the pipeline to generate orders.
        List<Point> priceInput = new ArrayList<>();
        for (Price p : prices) {
            priceInput.add(new Point((double)DateUtils.parseMonthAbbr(p.getDate()).getMillis(), p.getValue()));       // TODO: This could potentially be optimised.
        }

        // This loop supports the concept of generating multiple channels.
        // For each channel, there will exist a new set of lows and high points to create a new line.
        for (int h=0; h<prices.size(); h+=channelSize) {

            //Make a sublist based on the channel size.
            List<Point> partialPriceInput;
            if (h+channelSize<prices.size()) {
                partialPriceInput = priceInput.subList(h, h+channelSize);   // If there is still a channelSize size of points to traverse.
            } else {
                partialPriceInput = priceInput.subList(h, prices.size());  // If there is less than a channelSize size of points to traverse.
            }

            PointPair result = calculateLowsAndHighs(partialPriceInput, minWindowSize, variance);
            List<Point> lowPoints = result.getLows();
            List<Point> highPoints = result.getHighs();

            Channel channel = new Channel(lowPoints, highPoints);
            Line lowLine = channel.getLowLine();    //Returns a line of best fit given low points.
            Line highLine = channel.getHighLine();  //Returns a line of best fit given high points.

            // Calculate trade signals.
            tradeSignals = generateTradeSignals(tradeSignals, partialPriceInput, lowLine, highLine, threshold);
        }

        // Generate orders.
        OrderType nextStatus = OrderType.NOTHING; // The next status to look for.

        for (int i = 0; i < tradeSignals.size(); i++) {
            if (ordersGenerated.size() == 0) {
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

    private PointPair calculateLowsAndHighs(List<Point> priceInput, int minWindowSize, double variance) {
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
        PointPair result = new PointPair(highs, lows);
        return result;
    }

    private List<OrderType> generateTradeSignals(List<OrderType> tradeSignals, List<Point> priceInput, Line low, Line high, double threshold){
        for (int i=0; i<priceInput.size(); i++){
            if (tradeSignals.isEmpty()){
                tradeSignals.add(OrderType.NOTHING); // First day is always NOTHING.
                continue;
            }
            double pDate = priceInput.get(i).getX();
            double pPrice = priceInput.get(i).getY();
            double predictedLowPrice = GeometryUtils.predictedPrice(low, pDate);
            double predictedHighPrice = GeometryUtils.predictedPrice(high, pDate);

            //TODO: This code works even when high and low lines = NaN. Is this expected?
            if (pPrice >= predictedHighPrice-threshold) {
                tradeSignals.add(OrderType.SELL);
            } else if (pPrice <= predictedLowPrice+threshold) {
                tradeSignals.add(OrderType.BUY);
            } else {
                tradeSignals.add(OrderType.NOTHING);
            }
        }
        return tradeSignals;
    }
}
