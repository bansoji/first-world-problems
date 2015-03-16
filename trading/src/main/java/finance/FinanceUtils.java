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

    private static List<Double> calcAllReturns (List<Double> prices)
    {
        List<Double> returns = new ArrayList<Double>();
        // return at t = 0 is always 0.
        returns.add(0.);
        for (int i = 1; i < prices.size(); i++)
        {
            returns.add(calcReturns(prices.get(i),prices.get(i-1)));
        }
        return returns;
    }

    private static double calcSimpleMovingAvg(List<Double> returns, int n, int t)
    {
        double returnSum = 0;
        for (int i = t-n+1; i <= t; i++)
        {
            returnSum += returns.get(i);
        }
        return returnSum/n;
    }

    public static List<Double> calcAllSimpleMovingAvg(List<Double> prices, int n)
    {
        List<Double> simpleMovingAverages = new ArrayList<Double>();
        List<Double> returns = calcAllReturns(prices);
        for (int t = n-1; t < prices.size(); t++)
        {
            simpleMovingAverages.add(calcSimpleMovingAvg(returns,n,t));
        }
        return simpleMovingAverages;
    }
}
