import java.util.ArrayList;

/**
 * This file provides an interface for a Trading Strategy.
 */
public interface TradingStrategy {

    public void generateOrders();

    public ArrayList<Order> getOrders();

}
