package profit;

/**
 * Created by Zippy on 24/03/2015.
 */
public class ProfitUtils {

    public static double calcProfit (double prevPrice, double currPrice, long volume)
    {
        return (currPrice-prevPrice)*volume;
    }


    public static double calcAssets (double currPrice, long volume)
    {
        return currPrice*volume;
    }

    public static double calcPortfolio (double profit, double assets)
    {
        return profit + assets;
    }
}
