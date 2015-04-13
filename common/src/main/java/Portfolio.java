import java.util.*;

/**
 * Created by Edwin on 24/03/2015.
 * Modified by Banson on 28/03/2015.
 * This class represents the Portfolio of orders and buy/sell history.
 */
public class Portfolio {

    private History<Order> orderHistory; //Contains the history of Orders to work with

    private Map<String, List<Order>> boughtOrders; //Contains all orders bought.
    private Map<String, List<Order>> soldOrders; //Contains all orders sold.

    /**
     * This is the constructor for Portfolio. This also calls "Fill Portfolio".
     */
    public Portfolio (History<Order> orderHistory)
    {
        this.orderHistory = orderHistory;
        this.boughtOrders = new HashMap<String, List<Order>>();
        this.soldOrders = new HashMap<String, List<Order>>();
        this.FillPortfolio();
    }

    /**
     * This method will analyse the orderHistory and fill out the list in boughtOrders and soldOrders.
     */
    private void FillPortfolio ()
    {
        Set<String> names = orderHistory.getAllCompanies();
        for (String name : names)
        {
            boughtOrders.put(name, new ArrayList<Order>());
            soldOrders.put(name, new ArrayList<Order>());
            List<Order> companyHistory = orderHistory.getCompanyHistory(name);
            for (Order order : companyHistory)
            {
                if (order.getOrderType().equals(OrderType.BUY))
                {
                    buyOrder(name, order);
                } else {
                    sellOrder(name, order);
                }
            }
        }
    }
    
    /**
     * This method will store the order in boughtOrders.
     * @param company   The specified company the order is bought under.
     * @param order     The specified order to be bought.
     */
    private void buyOrder (String company, Order order)
    {
        boughtOrders.get(company).add(order);
    }

    /**
     * This method will store the order in soldOrders.
     * @param company   The specified company the order is sold under.
     * @param order     The specified order to be sold.
     */
    private void sellOrder (String company, Order order)
    {
        soldOrders.get(company).add(order);
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
    /*public double calcOrderValue ()
    {
        double totalValue = 0;
        for (Order order : this.ordersHolder)
        {
            totalValue += getValue(order.getVolume(), order.getPrice());
        }

        return totalValue;
    }*/

    /**
     * This method will calculate the returns off the buy/sell history in percent, actual value, as well as
     * short-sells/assets in possession. If there is selling of a non-existant asset, the asset value
     * should be negative.
     * @return  A list of values pertaining to financial returns.
     */
    /*public double calcReturns ()
    {
        double profit = 0;
        for (Order soldOrder : this.soldOrders.keySet())
        {
            profit += getValue(soldOrder.getVolume(), this.soldOrders.get(soldOrder) - soldOrder.getPrice());
        }

        return profit;
    }*/

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
