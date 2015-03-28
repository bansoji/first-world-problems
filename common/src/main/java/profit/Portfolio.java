package profit;
import com.sun.xml.internal.bind.v2.TODO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Edwin on 24/03/2015.
 * Modified by Banson on 28/03/2015.
 * This class represents the Portfolio of assets and buy/sell history.
 */
public class Portfolio {

    private static List<Asset> assetsHolder = new ArrayList<Asset>(); //Contains all assets currently in possession.
    // private static List<Asset> boughtAssets = new ArrayList<Asset>(); //A history of all the assets bought.
    private static Map<Asset, Double> soldAssets = new HashMap<Asset, Double>(); // A history of all the assets sold
                                                                                    // with their selling prices attached.

    /**
     * This method will "Buy" the asset (store it in portfolio, add to history)
     * @param asset     The specified asset to be bought.
     */
    public static void buyAsset (Asset asset)
    {
        assetsHolder.add(asset);
        // boughtAssets.add(asset);
    }

    /**
     * This method will "Sell" the asset (create the updated asset to add to history, and
     * remove the old one from the portfolio).
     * @param asset         The specified asset to sell.
     * @param sellPrice     The specified price to sell the asset at.
     * @param sellDate      The date the asset will be sold at.
     */
    public static void sellAsset (Asset soldAsset, double sellPrice, Date sellDate)
    {
        // Asset soldAsset = new Asset(asset.getCompanyName(), sellPrice, asset.getVolume(), sellDate);
        soldAssets.add(soldAsset, sellPrice);
        assetsHolder.remove(soldAsset);
    }

    /**
     * This method will clear the buy and sell history of the portfolio.
     */
    public static void clearHistory() {
        // boughtAssets.clear();
        soldAssets.clear();
    }

    /**
     * This method calculates the total value of all the assets currently in possession.
     * @return  The total value of all the assets in possession.
     */
    public static double calcAssetValue ()
    {
        double totalValue = 0;
        for (Asset asset : assetsHolder)
        {
            totalValue += getValue(asset.getVolume(), asset.getPrice());
        }

        return totalValue;
    }

    /**
     * This method calculates the total profit made off selling assets according to the history.
     * @return  The profit/loss of sold assets in comparison to their buy prices.
     */
    public static double calcProfit ()
    {
//        double expenditure = 0;
//        double revenue = 0;
//        for (Asset boughtAsset : boughtAssets)
//        {
//            expenditure += getValue(boughtAsset.getVolume(), boughtAsset.getPrice());
//        }
//
//        for (Asset soldAsset : soldAssets)
//        {
//            revenue += getValue(soldAsset.getVolume(), soldAsset.getPrice());
//        }
//
//        return revenue - expenditure;

        double profit = 0;
        for (Asset asset : soldAssets.keymap())
        {
            profit += soldAssets.get(asset) - asset.getPrice();
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

    /*public static double calcAssets (double currPrice, long volume)
    {
        return currPrice*volume;
    }

    public static double calcPortfolio (double profit, double assets)
    {
        return profit + assets;
    }
    */
}
