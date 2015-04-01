import java.util.*;

/**
 * Created by Edwin on 24/03/2015.
 * Modified by Banson on 28/03/2015.
 * This class represents the Portfolio of assets and buy/sell history.
 */
public class Portfolio {

    private History<Order> orderHistory; //Contains the history of Orders to work with

    private List<Order> ordersHolder; //Contains all orders currently in possession.
    private Map<Order, Double> soldOrders; // A history of all the orders sold with their selling prices attached.

    /**
     * This is the constructor for Portfolio.
     */
    public Portfolio (History<Order> orderHistory)
    {
        this.orderHistory = orderHistory;
        this.ordersHolder = new ArrayList<Order>();
        this.soldOrders = new HashMap<Order, Double>();
    }
    
    /**
     * This method will "Buy" the order and store it in ordersHolder.
     * @param order     The specified order to be bought.
     */
    private void buyOrder (Order order)
    {
        this.ordersHolder.add(order);
    }

    /**
     * This method will "Sell" the order (create the updated order to add to history, and
     * remove the old one from the portfolio).
     * @param soldOrder         The specified order to sell.
     * @param sellPrice     The specified price to sell the order at.
     */
    private void sellOrder (Order soldOrder, double sellPrice)
    {
        this.soldOrders.put(soldOrder, sellPrice);
        this.ordersHolder.remove(soldOrder);
    }

    /**
     * This method will clear the buy and sell history of the portfolio.
     */
    /*public void clearHistory() {
        this.soldOrders.clear();
    }*/

    /**
     * This method calculates the total value of all the orders currently in possession.
     * Note that this uses the price at the time that they were bought.
     * @return  The total value of all the orders in possession.
     */
    public double calcOrderValue ()
    {
        double totalValue = 0;
        for (Order order : this.ordersHolder)
        {
            totalValue += getValue(order.getVolume(), order.getPrice());
        }

        return totalValue;
    }

    /**
     * This method calculates the total profit made off selling orders according to the history.
     * Note that this does NOT include orders that are bought but not sold at the time of call.
     * @return  The profit/loss of sold orders in comparison to their buy prices.
     */
    public double calcProfit ()
    {
        double profit = 0;
        for (Order soldOrder : this.soldOrders.keySet())
        {
            profit += getValue(soldOrder.getVolume(), this.soldOrders.get(soldOrder) - soldOrder.getPrice());
        }

        return profit;
    }

    /**
     * This method calculates the individual value of an order in the portfolio.
     * @param volume    The number of units bought.
     * @param price     The price per unit.
     * @return          The value of the asset.
     */
    private static double getValue (int volume, double price)
    {
        return volume * price;
    }

}
