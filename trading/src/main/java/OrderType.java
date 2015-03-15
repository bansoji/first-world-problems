package main.java;

/**
 * This enum represents an Order type.
 */
public enum OrderType {
    BUY, SELL, NOTHING;

    private OrderType opposite;

    static {
        BUY.opposite = SELL;
        SELL.opposite = BUY;
    }

    public OrderType getOppositeOrderType(){
        return opposite;
    }
}
