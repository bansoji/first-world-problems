import java.util.ArrayList;

/**
 * An implementation of the Momentum Strategy.
 */
public class MomentumStrategy implements TradingStrategy {

    private ArrayList<Price> prices;
    private ArrayList<Order> ordersGenerated;

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

        // Calculate Returns.

        // Calculate Moving Average.

        // Calculate Trade Signals.

        // Generate the orders.

    }

    /**
     * Gets all the orders that have been generated using this strategy.
     * @return an ArrayList of Orders.
     */
    @Override
    public ArrayList<Order> getOrders() {
        return ordersGenerated;
    }
}
