import java.util.List;

/**
 * This file provides an interface for a Trading Strategy.
 */
public interface TradingStrategy {

    public void generateOrders();

    public List<Order> getOrders();

    public void setMovingAverage(int movingAverage);

    public void setVolume(int volume);

    public void setThreshold(double threshold);

}
