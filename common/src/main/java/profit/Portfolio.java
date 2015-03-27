package profit;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edwin on 24/03/2015.
 */
public class Portfolio {

    private static List<Asset> assetsHolder = new ArrayList<Asset>(); //Contains all assets currently in possession.
    private static List<Asset> boughtAssets = new ArrayList<Asset>(); //A history of all the assets bought.
    private static List<Asset> soldAssets = new ArrayList<Asset>(); // A history of all the assets sold.


    public static void buyAsset (Asset asset)
    {
        assetsHolder.add(asset);
        boughtAssets.add(asset);
    }

    public static void sellAsset (Asset asset, double sellPrice)
    {
        Asset soldAsset = new Asset(asset.getCompanyName(), sellPrice, asset.getVolume());
        soldAssets.add(soldAsset);
        assetsHolder.remove(asset);
    }

    public static void clearHistory() {
        boughtAssets.clear();
        soldAssets.clear();
    }

    public static double calcAssetValue ()
    {
        double totalValue = 0;
        for (Asset asset : assetsHolder)
        {
            totalValue += getValue(asset.getVolume(), asset.getPrice());
        }

        return totalValue;
    }


    public static double calcProfit ()
    {
        double expenditure = 0;
        double revenue = 0;
        for (Asset boughtAsset : boughtAssets)
        {
            expenditure += getValue(boughtAsset.getVolume(), boughtAsset.getPrice());
        }

        for (Asset soldAsset : soldAssets)
        {
            revenue += getValue(soldAsset.getVolume(), soldAsset.getPrice());
        }

        return revenue - expenditure;
    }

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
