package profit;


import main.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zippy on 19/05/2015.
 */
public class OptimalProfit {

    private List<Price> priceHistory; //The given list of prices to work with.
    private History<Order> optimalOrders; //Something to store orders for portfolio.
    private Portfolio optimalPortfolio; //The portfolio containing the optimalOrders.

    private boolean paired = true; //Pairing flag.
    private int volume; //Volume of the order.

    /**
     * The constructor for the OptimalProfit class.
     * @param priceHistory  The data of prices given to analyse.
     */
    public OptimalProfit(List<Price> priceHistory) {
        this.priceHistory = priceHistory;
        volume = 100;
        optimalOrders = new History<>();
        generateOrders();
        optimalPortfolio = new Portfolio (optimalOrders, null, null);
    }


    /**
     * This method will act as the 'strategy' to create orders that will return the optimal profit.
     * Uses the local minima/maxima strategy.
     */
    private void generateOrders() {
        Price prev = null;
        boolean nextBuy = true;
        for (Price price : priceHistory) {
            if (priceHistory.get(0).equals(prev)) {
                if (prev.getValue() < price.getValue()) {
                    buy(prev);
                    nextBuy = false;
                    updatePair();
                }
                if (prev.getValue() > price.getValue()) {
                    sell(prev);
                    nextBuy = true;
                    updatePair();
                }
            } else {
                if (prev.getValue() < price.getValue() && nextBuy) {
                    buy(prev);
                    nextBuy = false;
                    updatePair();
                }
                if (prev.getValue() > price.getValue() && !nextBuy) {
                    sell(prev);
                    nextBuy = true;
                    updatePair();
                }
            }
            prev = price;
        }

        if (paired) {
            if (nextBuy) {
                buy(prev);
            } else {
                sell(prev);
            }
        }

    }

    private void updatePair() {
        if (paired) {
            paired = false;
        } else {
            paired = true;
        }
    }

    private void buy(Price prev) {
        Order buy = new Order(OrderType.BUY, prev.getCompanyName(), prev.getValue(), volume, prev.getDate());
        optimalOrders.add("Company", buy);
    }

    private void sell(Price prev) {
        Order sell = new Order(OrderType.SELL, prev.getCompanyName(), prev.getValue(), volume, prev.getDate());
        optimalOrders.add("Company", sell);
    }

    /**
     * Use this method to get the list of type Profit.
     * @return  The list of Profit.
     */
    public List<Profit> getProfitList() {
        return optimalPortfolio.getProfitList();
    }
}
