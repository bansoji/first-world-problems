package main.java;

/**
 * Created by addo on 10/03/15.
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
