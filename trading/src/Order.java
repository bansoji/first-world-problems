import java.util.Date;

/**
 *
 */
public class Order {
    private OrderType signal;
    private String ric; // RIC (Name) of the Order.
    private double price; // The price of the Order to place.
    private int volume; // The number of items to trade.
    private Date date; // The date of the Order.

    /**
     * Creates a new Order.
     * @param signal    The signal (buy or sell).
     * @param ric       The ID of the share.
     * @param price     The price of a share.
     * @param volume    The number of shares to buy or sell.
     * @param date      The date of the order.
     */
    public Order(OrderType signal, String ric, double price, int volume, Date date){
        this.signal = signal;
        this.ric = ric;
        this.price = price;
        this.volume = volume;
        this.date = date;
    }


    /**
     * Gets the price of a single share.
     * @return the price of a single share of this Order.
     */
    public double getPrice(){
        return this.price;
    }

    /**
     * Returns the total price of the Order.
     * @return total price of the Order.
     */
    public double totalPrice(){
        return this.price * this.volume;
    }

    /**
     * Returns the total amount of the transaction being made.
     * @return The total transaction value. Negative for purchases, positive for sale.
     */
    public double totalTransactionValue(){
        if (this.signal == OrderType.BUY){
            return this.totalPrice() * -1;
        } else {
            return this.totalPrice();
        }
    }

    /**
     * Returns the Order date.
     * @return a Date object corresponding to the date of the file.
     */
    public Date getOrderDate(){
        return this.date;
    }

}
