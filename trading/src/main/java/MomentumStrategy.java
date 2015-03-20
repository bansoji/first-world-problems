import finance.FinanceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An implementation of the Momentum Strategy.
 */
public class MomentumStrategy implements TradingStrategy {

    private List<Price> prices;
    private List<Order> ordersGenerated;
    private static final int MOVING_AVERAGE = 4;
    private static final double THRESHOLD = 0.001;
    private static final int VOLUME = 100; // Set by MSM Spec.

    private static final Logger logger = Logger.getLogger("log");

    public MomentumStrategy(List<Price> historicalPrices){
        this.prices = historicalPrices;
        this.ordersGenerated = new ArrayList<Order>();
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
            System.out.println(p.getValue());
        }

        List<Double> sma = FinanceUtils.calcAllSimpleMovingAvg(priceInput, MOVING_AVERAGE);

        // Calculate Trade Signals.
        List<OrderType> tradeSignals = generateTradeSignals(sma, THRESHOLD);

        for (Double d : sma){
            System.out.println(d);
        }

        for (OrderType s : tradeSignals){
            System.out.println(s);
        }

        // Generate the orders.
        OrderType nextStatus = OrderType.BUY; // The next status to look for.
        for (int i=0; i<tradeSignals.size(); i++){
            if (tradeSignals.get(i).equals(nextStatus)){
                // Create an order using this ith day.
                Price tradePrice = prices.get(i + MOVING_AVERAGE-1); // Get the price for that day. Offset by moving average.
                // TODO(Addo): Account for missing dates in the line above.
                Order o = new Order(nextStatus, tradePrice.getCompanyName(), tradePrice.getValue(), VOLUME,
                                    tradePrice.getDate());
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
}
