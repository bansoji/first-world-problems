package main;

import java.util.*;

/**
 * Created by Edwin on 24/03/2015.
 * Modified by Banson on 28/03/2015.
 * Updated by Edwin on 14/04/2015.
 * This class represents the Portfolio of orders and buy/sell history.
 */
public class Portfolio {

    private History<Order> orderHistory; //Contains the history of Orders to work with

    private Map<String, List<Order>> boughtOrders; //Contains all orders bought.
    private Map<String, List<Order>> soldOrders; //Contains all orders sold.

    private Map<String, List<Double>> returns; //Contains the return data for each company.
    private Map<String, Double> assetValue; //Contains the asset value data for each company.

    private double totalBuyValue;
    private double totalSellValue;
    private double totalReturnValue;

    /**
     * This is the constructor for Portfolio. This also calls "Fill Portfolio".
     */
    public Portfolio (History<Order> orderHistory)
    {
        this.orderHistory = orderHistory;
        boughtOrders = new HashMap<>();
        soldOrders = new HashMap<>();
        returns = new HashMap<>();
        assetValue = new HashMap<>();
        FillPortfolio();
    }

    /**
     * This method will analyse the orderHistory, filling the portfolio with orders while simultaneously
     * calculating the return values and asset values using appropriate buy/sell pairs.
     */
    private void FillPortfolio ()
    {
        for (String name : orderHistory.getAllCompanies())
        {
            //initialisation of variables for this particular company.
            boughtOrders.put(name, new ArrayList<>());
            soldOrders.put(name, new ArrayList<>());
            returns.put(name, new ArrayList<>());
            returns.get(name).add(0, 0.00);
            returns.get(name).add(1, 0.00);
            returns.get(name).add(2, 0.00);
            double valueNumber = 0.00;

            List<Order> companyHistory = orderHistory.getCompanyHistory(name);
            for (Order order : companyHistory)
            {
                if (order.getOrderType().equals(OrderType.BUY)) {
                    buyOrder(name, order);
                } else {
                    sellOrder(name, order);
                }
            }

            if (boughtOrders.get(name).size() > 0) {
                for (Order order : boughtOrders.get(name))
                {
                    valueNumber += order.getValue();
                }
            } else if (soldOrders.get(name).size() > 0) {
                for (Order order : soldOrders.get(name))
                {
                    valueNumber -= order.getValue();
                }
            }

            assetValue.put(name, valueNumber);
        }
    }
    
    /**
     * This method will store the order in boughtOrders, or calculate Sell/Buy pair if a soldOrder exists.
     * @param company   The specified company the order is bought under.
     * @param order     The specified order to be bought.
     */
    private void buyOrder (String company, Order order)
    {
        totalBuyValue += order.getValue();
        if (soldOrders.get(company).size() == 0) {
            boughtOrders.get(company).add(order);
        } else {
            Order sellingOrder = soldOrders.get(company).get(0);
            double sellValue = sellingOrder.getValue();
            double buyValue = order.getValue();
            double returnValue = sellValue - buyValue;
            double returnPercent = returnValue / sellValue; //short-selling change - divide by sell
            totalReturnValue += returnValue;

            addReturns(company, returnValue, returnPercent, buyValue);
            soldOrders.get(company).remove(0);
        }
    }

    /**
     * This method will store the order in soldOrders, or calculate Buy/Sell pair if boughtOrder exists.
     * @param company   The specified company the order is sold under.
     * @param order     The specified order to be sold.
     */
    private void sellOrder (String company, Order order)
    {
        totalSellValue += order.getValue();
        if (boughtOrders.get(company).size() == 0) {
            soldOrders.get(company).add(order);
        } else {
            Order buyingOrder = boughtOrders.get(company).get(0);
            double buyValue = buyingOrder.getValue();
            double sellValue = order.getValue();
            double returnValue = sellValue - buyValue;
            double returnPercent = returnValue / buyValue; //regular-selling - divide by buy
            totalReturnValue += returnValue;

            addReturns(company, returnValue, returnPercent, buyValue);
            boughtOrders.get(company).remove(0);
        }
    }

    /**
     * Returns a map of all the companies' return values and percents.
     * @return The map of companies' return values and percents.
     */
    public Map<String, List<Double>> getReturns ()
    {
        return returns;
    }

    /**
     * Returns a map of all the companies' asset values.
     * @return The map of companies' asset values.
     */
    public Map<String, Double> getAssetValue ()
    {
        return assetValue;
    }

    public Map<String, Double> getPortfolioValue ()
    {
        Map<String, Double> portfolioValue = new HashMap<>();
        for (String name : orderHistory.getAllCompanies())
        {
            portfolioValue.put(name, returns.get(name).get(0) + assetValue.get(name));
        }

        return portfolioValue;
    }

    /**
     * This method will add the returnValue and returnPercent calculated in buyOrder/sellOrder for a company.
     * @param company           The company name.
     * @param returnValue       The specified returnValue.
     * @param returnPercent     The specified returnPercent.
     */
    private void addReturns (String company, double returnValue, double returnPercent, double bought)
    {
        double totalReturn = returns.get(company).get(0);
        double totalPercent = returns.get(company).get(1);
        double totalBought = returns.get(company).get(2);
        totalReturn += returnValue;
        totalPercent += returnPercent;
        totalBought += bought;
        returns.get(company).set(0, totalReturn);
        returns.get(company).set(1, totalPercent);
        returns.get(company).set(2, totalBought);
    }

    public double getTotalBuyValue() {
        return totalBuyValue;
    }

    public double getTotalSellValue() {
        return totalSellValue;
    }

    public double getTotalReturnValue() {
        return totalReturnValue;
    }
}