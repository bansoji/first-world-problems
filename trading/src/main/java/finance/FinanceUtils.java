package finance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavin Tam on 10/03/15.
 */

public class FinanceUtils {
    private static double calcReturns(double currPrice, double prevPrice)
    {
        return (currPrice - prevPrice)/prevPrice;
    }

    private static double calcSimpleMovingAvg(List<Double> prices, int n, int t)
    {
        //if we want the SMA over n prices at a particular t, n <= t
        if (t < n) return -1;

        int start = t-n;
        int end = t;
        double returnsSum = 0;
        for (int i = start; i < end; i++)
        {
            if (i-1 >= 0) {
                returnsSum += calcReturns(prices.get(i), prices.get(i - 1));
            }
        }
        return returnsSum/n;
    }

    public static List<Double> calcAllSimpleMovingAvg(List<Double> prices, int n)
    {
        List<Double> simpleMovingAverages = new ArrayList<>();
        for (int t = n; t <= prices.size(); t++)
        {
            simpleMovingAverages.add(calcSimpleMovingAvg(prices,n,t));
        }
        return simpleMovingAverages;
    }
}
