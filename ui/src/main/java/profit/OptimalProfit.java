package profit;


import main.*;

import java.util.List;

/**
 * Created by Zippy on 19/05/2015.
 * A class which calculates the optimal profit given a list of prices.
 */
public class OptimalProfit {

    //private List<Price> priceHistory; //The given list of prices to work with.
    private History<Price> priceHistory; //The given list of prices to work with.
    private History<Order> optimalOrders; //Something to store orders for portfolio.
    private Portfolio optimalPortfolio; //The portfolio containing the optimalOrders.
    
    private int volume; //Volume of the order.

    /**
     * The constructor for the OptimalProfit class.
     * @param priceHistory  The data of prices given to analyse.
     */
    public OptimalProfit(List<Price> givenPrices) {
        priceHistory = new History<>();
        optimalOrders = new History<>();
        volume = 100;
        fillHistory(givenPrices);
        generateAllOrders();
        optimalPortfolio = new Portfolio (optimalOrders, null, null);
    }


    /**
     * This method will split the price list into individual companies for processing.
     * @param givenPrices //The list of prices given.
     */
    private void fillHistory(List<Price> givenPrices) {
        for (Price price : givenPrices) {
            priceHistory.add(price.getCompanyName(), price);
        }
    }

    /**
     * This method iterates through each company, calling generateOrders.
     */
    private void generateAllOrders() {
        for (String individual : priceHistory.getAllCompanies()) {
            generateOrders(priceHistory.getCompanyHistory(individual));
        }
    }

    /**
     * This method will act as the 'strategy' to create orders that will return the optimal profit.
     * Uses the local minima/maxima strategy.
     */
    private void generateOrders(List<Price> individualPrices) {
        Price prev = null;
        boolean nextBuy = true;
        boolean paired = true;
        for (Price price : individualPrices) {
            if (individualPrices.get(0).equals(prev)) {
                if (prev.getValue() < price.getValue()) {
                    buy(prev);
                    nextBuy = false;
                    paired = !paired;
                }
                if (prev.getValue() > price.getValue()) {
                    sell(prev);
                    nextBuy = true;
                    paired = !paired;
                }
            } else if (prev != null) {
                if (prev.getValue() < price.getValue() && nextBuy) {
                    buy(prev);
                    nextBuy = false;
                    paired = !paired;
                }
                if (prev.getValue() > price.getValue() && !nextBuy) {
                    sell(prev);
                    nextBuy = true;
                    paired = !paired;
                }
            }
            prev = price;
        }

        if (!paired) {
            if (nextBuy) {
                buy(prev);
            } else {
                sell(prev);
            }
        }

    }


    /**
     * Helper function to 'buy' a Price.
     * @param prev
     */
    private void buy(Price prev) {
        Order buy = new Order(OrderType.BUY, prev.getCompanyName(), prev.getValue(), volume, prev.getDate());
        optimalOrders.add(prev.getCompanyName(), buy);
    }

    /**
     * Helper function to 'sell' a Price.
     * @param prev
     */
    private void sell(Price prev) {
        Order sell = new Order(OrderType.SELL, prev.getCompanyName(), prev.getValue(), volume, prev.getDate());
        optimalOrders.add(prev.getCompanyName(), sell);
    }

    /**
     * Use this method to get the list of type Profit.
     * @return  The list of Profit.
     */
    public List<Profit> getProfitList() {
        return optimalPortfolio.getProfitList();
    }
}
