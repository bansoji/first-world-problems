package core;

/**
 * This enum represents an Order type.
 */
public enum OrderType {
    BUY, SELL, NOTHING;

    private OrderType opposite;
    private OrderType symbol;

    static {
        BUY.opposite = SELL;
        SELL.opposite = BUY;
    }

    public OrderType getOppositeOrderType(){
        return opposite;
    }

    public String getSignal(OrderType t){
        if(t == OrderType.BUY) {
            return "B";
        } else if (t == OrderType.SELL){
            return "S";
        } else {
            return "";
        }
    }

    public String toString(){
        if(this == OrderType.BUY) {
            return "BUY";
        } else if (this == OrderType.SELL){
            return "SELL";
        } else {
            return "";
        }
    }
}
