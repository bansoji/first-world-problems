package main;

import org.joda.time.DateTime;

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

    private Map<String, Returns> returns; //Contains the return data for each company.
    private Map<String, Double> assetValue; //Contains the asset value data for each company.
    private List<Profit> profitList; //Contains the buy/sell points with date and return.
    private Map<String, List<Profit>> companyProfitList;
    private Map<String, Double> companyReturns;

    private double totalBuyValue;
    private double totalSellValue;
    private double totalReturnValue;

    /**
     * This is the constructor for Portfolio. This also calls "Fill Portfolio".
     */
    public Portfolio (History<Order> orderHistory, DateTime startDate, DateTime endDate)
    {
        this.orderHistory = orderHistory;
        boughtOrders = new HashMap<>();
        soldOrders = new HashMap<>();
        returns = new HashMap<>();
        assetValue = new HashMap<>();
        profitList = new ArrayList<>();
        companyProfitList = new HashMap<>();
        companyReturns = new HashMap<>();
        FillPortfolio();
        //profit is always 0 at the start date of prices data
        if (startDate != null) {
            profitList.add(new Profit(0, startDate));
            for (String company: companyProfitList.keySet()) {
                companyProfitList.get(company).add(new Profit(0, startDate));
            }
        }
        //profit is always the last calculation of the total return value at the end date of prices data
        if (endDate != null) {
            profitList.add(new Profit(totalReturnValue, endDate));
            for (String company: companyProfitList.keySet()) {
                companyProfitList.get(company).add(new Profit(companyReturns.get(company), endDate));
            }
        }
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
            returns.put(name, new Returns());
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

            profitList.add(new Profit(totalReturnValue, order.getOrderDate()));
            if (!companyProfitList.containsKey(company)) {
                companyProfitList.put(company, new ArrayList<>());
                companyReturns.put(company,0.);
            }
            companyReturns.put(company,companyReturns.get(company)+returnValue);
            companyProfitList.get(company).add(new Profit(companyReturns.get(company), order.getOrderDate()));

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

            profitList.add(new Profit(totalReturnValue, order.getOrderDate()));
            if (!companyProfitList.containsKey(company)) {
                companyProfitList.put(company, new ArrayList<>());
                companyReturns.put(company,0.);
            }
            companyReturns.put(company,companyReturns.get(company)+returnValue);
            companyProfitList.get(company).add(new Profit(companyReturns.get(company), order.getOrderDate()));

            addReturns(company, returnValue, returnPercent, buyValue);
            boughtOrders.get(company).remove(0);
        }
    }

    /**
     * Returns a map of all the companies' return values and percents.
     * @return The map of companies' return values and percents.
     */
    public Map<String, Returns> getReturns ()
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

    /**
     * Returns a map of all the companies' total values (asset + return value)
     * @return  The map of all companies' portfolio values.
     */
    public Map<String, Double> getPortfolioValue ()
    {
        Map<String, Double> portfolioValue = new HashMap<>();
        for (String name : orderHistory.getAllCompanies())
        {
            portfolioValue.put(name, returns.get(name).getReturns() + assetValue.get(name));
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
        returns.get(company).addReturns(returnValue);
        returns.get(company).addPercent(returnPercent);
        returns.get(company).addBought(bought);
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

    public List<Profit> getProfitList() {
        return profitList;
    }

    public List<Profit> getCompanyProfitList(String company) {
        return companyProfitList.get(company);
    }

    public double getCompanyReturns(String company) {
        return companyReturns.get(company);
    }

    public Set<String> getCompanies() {
        return companyReturns.keySet();
    }

    public History<Order> getOrderHistory() {
        return orderHistory;
    }

    public boolean isEmpty() {
        return (orderHistory.getAllCompanies().size() == 0);
    }
}
