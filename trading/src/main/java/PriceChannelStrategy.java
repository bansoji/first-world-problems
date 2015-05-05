import quickDate.Order;
import quickDate.Price;

import java.io.InputStream;
import java.util.List;

/**
 * Created by jasonlim on 5/05/15.
 */
public class PriceChannelStrategy implements TradingStrategy {
    public PriceChannelStrategy(List<Price> historicalPrices, InputStream config) {

    }

    @Override
    public void generateOrders() {

    }

    @Override
    public List<Order> getOrders() {
        return null;
    }
}
