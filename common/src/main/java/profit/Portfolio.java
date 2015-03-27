package profit;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edwin on 24/03/2015.
 */
public class Portfolio {

    private static List<Asset> portfolio = new ArrayList<Asset>();
    private static double balance = 0;



    public static void addAsset (Asset asset, double cost)
    {
        portfolio.add(asset);
        balance -= cost;
    }

    public static void removeAsset (Asset asset, double value)
    {
        portfolio.remove(asset);
        balance += value;
    }

    public static double calcAssetValue ()
    {
        double totalValue = 0;
        for (Asset individualAsset : portfolio)
        {
            totalValue ++; //placeholder code
        }

        return totalValue;
    }

    public static double returnBalance ()
    {
        return balance;
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
