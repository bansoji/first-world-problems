package core;

import date.DateUtils;
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
    private List<Long> dates;
    private Map<String, List<Profit>> companyProfitList;
    private Map<String, List<Profit>> changeInProfit;

    private double totalBuyValue;
    private double totalSellValue;
    private double totalReturnValue;

    private DateTime endDate; // The last date.
    private List<Order> endDateOrders; // Orders that occurred on the endDate.

    /**
     * This is the constructor for Portfolio. This also calls "Fill Portfolio".
     */
    public Portfolio (History<Order> orderHistory, DateTime startDate, DateTime endDate)
    {
        this.orderHistory = orderHistory;
        this.endDate = endDate;
        boughtOrders = new HashMap<>();
        soldOrders = new HashMap<>();
        returns = new HashMap<>();
        assetValue = new HashMap<>();
        profitList = new ArrayList<>();
        companyProfitList = new HashMap<>();
        changeInProfit = new HashMap<>();
        endDateOrders = new ArrayList<>();
        HashSet<Long> dates = new HashSet<>();
        for (String company: orderHistory.getAllCompanies()) {
            List<Order> orders = orderHistory.getCompanyHistory(company);
            for (Order order: orders) {
                dates.add(order.getOrderDate().getMillis());
            }
        }
        this.dates = new ArrayList<>(dates);
        Collections.sort(this.dates);
        FillPortfolio();
        //profit is always 0 at the start date of prices data
        if (startDate != null) {
            for (String company: companyProfitList.keySet()) {
                companyProfitList.get(company).add(0, new Profit(0, 0, startDate));
            }
        }
        //profit is always the last calculation of the total return value at the end date of prices data
        if (endDate != null) {
            for (String company: companyProfitList.keySet()) {
                companyProfitList.get(company).add(new Profit(returns.get(company).getReturns(), returns.get(company).getPercent(), endDate));
            }
        }
        if (endDate != null && startDate != null)
            calculateProfitList(startDate, endDate);
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

            // If an order exists and it occurs on the last day (endDate)

            if (companyHistory.size() > 0 && companyHistory.get(companyHistory.size()-1).getOrderDate().equals(endDate)){
                endDateOrders.add(companyHistory.get(companyHistory.size()-1));
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
            Order sellingOrder = soldOrders.get(company).remove(0);
            double sellValue = sellingOrder.getValue();
            double buyValue = order.getValue();
            double returnValue = sellValue - buyValue;
            double returnPercent = returnValue / sellValue; //short-selling change - divide by sell
            totalReturnValue += returnValue;

            if (!companyProfitList.containsKey(company)) {
                companyProfitList.put(company, new ArrayList<>());
                changeInProfit.put(company, new ArrayList<>());
            }
            double oldReturns = returns.get(company).getReturns();
            double oldBuyVolume = returns.get(company).getBought();
            addReturns(company, returnValue, returnPercent, buyValue);
            changeInProfit.get(company).add(new Profit(returns.get(company).getReturns()-oldReturns, 0, order.getOrderDate()));
            companyProfitList.get(company).add(new Profit(returns.get(company).getReturns(), returns.get(company).getPercent(), order.getOrderDate()));
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
            Order buyingOrder = boughtOrders.get(company).remove(0);
            double buyValue = buyingOrder.getValue();
            double sellValue = order.getValue();
            double returnValue = sellValue - buyValue;
            double returnPercent = returnValue / buyValue; //regular-selling - divide by buy
            totalReturnValue += returnValue;

            if (companyProfitList.get(company) == null) {
                companyProfitList.put(company, new ArrayList<>());
                changeInProfit.put(company, new ArrayList<>());
            }
            double oldReturns = returns.get(company).getReturns();
            addReturns(company, returnValue, returnPercent, buyValue);
            //TODO fix percent
            changeInProfit.get(company).add(new Profit(returns.get(company).getReturns()-oldReturns, -1, order.getOrderDate()));
            companyProfitList.get(company).add(new Profit(returns.get(company).getReturns(), returns.get(company).getPercent(), order.getOrderDate()));
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

    private void calculateProfitList(DateTime start, DateTime end) {
        profitList = new ArrayList<>();
        profitList.add(new Profit(0,0,start));
        for (Long time: dates) {
            double totalReturnValue = 0;
            double totalBuyValue = 0;
            for (String company : changeInProfit.keySet()) {
                for (Profit p : changeInProfit.get(company)) {
                    if (p.getProfitDate().equals(time) || p.getProfitDate().isBefore(time)) {
                        totalReturnValue += p.getProfitValue();
                        totalBuyValue += 100;
                    }
                }
            }
            profitList.add(new Profit(totalReturnValue, totalReturnValue / totalBuyValue, new DateTime(time)));
        }
        profitList.add(new Profit(totalReturnValue,totalReturnValue/totalBuyValue,end));
    }

    public List<Profit> getCompanyProfitList(String company) {
        return companyProfitList.get(company);
    }

    public Returns getCompanyReturns(String company) {
        return returns.get(company);
    }

    public Set<String> getCompanies() {
        return returns.keySet();
    }

    public History<Order> getOrderHistory() {
        return orderHistory;
    }

    public boolean isEmpty() {
        return (orderHistory.getAllCompanies().size() == 0);
    }

    public List<Order> getEndDateOrders() {
        return endDateOrders;
    }

    /**
     * Gets the last date of the portfolio.
     * @return
     */
    public DateTime getEndDate(){
        return endDate;
    }
}
