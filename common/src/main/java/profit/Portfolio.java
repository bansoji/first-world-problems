package profit;
import java.util.*;

/**
 * Created by Edwin on 24/03/2015.
 * Modified by Banson on 28/03/2015.
 * This class represents the Portfolio of assets and buy/sell history.
 */
public class Portfolio {

    private static List<Asset> assetsHolder = new ArrayList<Asset>(); //Contains all assets currently in possession.
    private static Map<Asset, Double> soldAssets = new HashMap<Asset, Double>(); // A history of all the assets sold
                                                                                    // with their selling prices attached.

    /**
     * This method will "Buy" the asset and store it in assetsHolder.
     * @param asset     The specified asset to be bought.
     */
    public static void buyAsset (Asset asset)
    {
        assetsHolder.add(asset);
    }

    /**
     * This method will "Sell" the asset (create the updated asset to add to history, and
     * remove the old one from the portfolio).
     * @param soldAsset         The specified asset to sell.
     * @param sellPrice     The specified price to sell the asset at.
     */
    public static void sellAsset (Asset soldAsset, double sellPrice)
    {
        soldAssets.put(soldAsset, sellPrice);
        assetsHolder.remove(soldAsset);
    }

    /**
     * This method will clear the buy and sell history of the portfolio.
     */
    public static void clearHistory() {
        soldAssets.clear();
    }

    /**
     * This method calculates the total value of all the assets currently in possession.
     * Note that this uses the price at the time that they were bought.
     * @return  The total value of all the assets in possession.
     */
    public static double calcAssetValue ()
    {
        double totalValue = 0;
        for (Asset asset : assetsHolder)
        {
            totalValue += getValue(asset.getVolume(), asset.getBuyPrice());
        }

        return totalValue;
    }

    /**
     * This method calculates the total profit made off selling assets according to the history.
     * Note that this does NOT include Assets that are bought but not sold at the time of call.
     * @return  The profit/loss of sold assets in comparison to their buy prices.
     */
    public static double calcProfit ()
    {
        double profit = 0;
        for (Asset soldAsset : soldAssets.keySet())
        {
            profit += getValue(soldAsset.getVolume(), soldAssets.get(soldAsset) - soldAsset.getBuyPrice());
        }

        return profit;
    }

    /**
     * This method calculates the individual value of an asset in the portfolio.
     * @param volume    The number of units bought.
     * @param price     The price per unit.
     * @return          The value of the asset.
     */
    private static double getValue (int volume, double price)
    {
        return volume * price;
    }

}
