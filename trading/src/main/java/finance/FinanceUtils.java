package finance;

import java.util.List;

/**
 * Created by Gavin Tam on 10/03/15.
 */

public class FinanceUtils {
    private static double calcReturns(double currPrice, double prevPrice)
    {
        return (currPrice - prevPrice)/prevPrice;
    }

    public static double calcSimpleMovingAverage(List<Double> prices, int n)
    {
        int t = prices.size();
        //if we want the SMA at a particular t, there must be at least t elements
        if (t < n) return -1;

        int start = t-n;
        int end = t;
        double returnsSum = 0;
        for (int i = start+1; i < end; i++)
        {
            returnsSum += calcReturns(prices.get(i),prices.get(i-1));
        }
        return returnsSum/n;
    }
}
