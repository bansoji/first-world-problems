import quickDate.Order;

import java.util.List;

/**
 * This file provides an interface for a Trading Strategy.
 */
public interface TradingStrategy {

    public void generateOrders();

    public List<Order> getOrders();

}
