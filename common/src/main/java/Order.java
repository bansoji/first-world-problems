import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * This class represents the an Order that is generated by a trading strategy.
 */
public class Order {

    private OrderType signal;
    private String companyName; // RIC (Name) of the Order.
    private double price; // The price of the Order to place.
    private int volume; // The number of items to trade.
    private Date date; // The date of the Order.

    private static final Logger logger = Logger.getLogger("log");

    /**
     * Creates a new Order.
     * @param signal            The signal (buy or sell).
     * @param companyName       The ID of the company, identical to the input file.
     * @param price             The price of a share.
     * @param volume            The number of shares to buy or sell.
     * @param date              The date of the order.
     */
    public Order(OrderType signal, String companyName, double price, int volume, Date date){
        this.signal = signal;
        this.companyName = companyName;
        this.price = price;
        this.volume = volume;
        this.date = date;
    }

    /**
     * Gets the company name. This is identical to the RIC from the input file.
     * @return a String containing the Company Name.
     */
    public String getCompanyName(){
        return this.companyName;
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
    public double getValue(){
        return this.price * this.volume;
    }

    /**
     * Returns the total amount of the transaction being made.
     * @return The total transaction value. Negative for purchases, positive for sale.
     */
    public double totalTransactionValue(){
        if (this.signal == OrderType.BUY){
            return this.getValue() * -1;
        } else {
            return this.getValue();
        }
    }

    public OrderType getOrderType()
    {
        return signal;
    }

    /**
     * Returns the Order date.
     * @return a Date object corresponding to the date of the file.
     */
    public Date getOrderDate(){
        return this.date;
    }

    public String[] toStringArray() {
        DateFormat df = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss");
        String[] s = new String[6];
        s[0] = this.companyName;
        s[1] = df.format(this.date);
        s[2] = String.valueOf(this.price);
        s[3] = String.valueOf(this.volume);
        s[4] = String.valueOf(this.price);
        s[5] = this.signal.getSignal(this.signal);
        return s;
    }

}