package main.java;

import main.java.finance.FinanceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the Momentum Strategy.
 */
public class MomentumStrategy implements TradingStrategy {

    private ArrayList<Price> prices;
    private ArrayList<Order> ordersGenerated;
    private static final int MOVING_AVERAGE = 4;
    private static final double THRESHOLD = 0.001;
    private static final int VOLUME = 100; // Set by MSM Spec.

    public MomentumStrategy(ArrayList<Price> historicalPrices){
        this.prices = historicalPrices;
        this.ordersGenerated = new ArrayList<Order>();
    }

    /**
     * Generate orders using this strategy.
     */
    @Override
    public void generateOrders() {
        // This will trigger the pipeline to generate orders.
        ArrayList<Double> priceInput = new ArrayList<Double>();
        for (Price p : prices){
            priceInput.add(p.getValue());       // TODO: This could potentially be optimised.
        }

        List<Double> sma = FinanceUtils.calcAllSimpleMovingAvg(priceInput, MOVING_AVERAGE);

        // Calculate Trade Signals.
        List<OrderType> tradeSignals = generateTradeSignals(sma, THRESHOLD);

        // Generate the orders.
        OrderType nextStatus = OrderType.BUY; // The next status to look for.
        for (int i=0; i<tradeSignals.size(); i++){
            if (tradeSignals.get(i).equals(nextStatus)){
                // Create an order using this ith day.
                Price tradePrice = prices.get(i + MOVING_AVERAGE); // Offset by moving average.
                Order o = new Order(nextStatus, tradePrice.getCompanyName(), tradePrice.getValue(), VOLUME,
                                    tradePrice.getDate());
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
    public ArrayList<Order> getOrders() {
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
            if (difference > THRESHOLD){
                l.add(OrderType.BUY);
            } else if (difference < THRESHOLD * -1) {
                l.add(OrderType.SELL);
            } else {
                l.add(OrderType.NOTHING);
            }
        }

        return l;
    }
}
