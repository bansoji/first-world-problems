package utils;


import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;
import org.joda.time.Days;

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
        for (int t = n; t < prices.size(); t++)
        {
            simpleMovingAverages.add(calcSimpleMovingAvg(returns,n,t));
        }
        return simpleMovingAverages;
    }

    public static double calcProfit (double prevPrice, double currPrice, long volume)
    {
        return (currPrice-prevPrice)*volume;
    }

    /**
     * Calculates the Sharpe Ratio for a particular strategy's performance on a particular company over a period of time.
     * @param returns       The list of individual returns given by a strategy over a period of time (for this company).
     * @param startDate     The starting date the strategy is used.
     * @param endDate       The ending date the strategy stops at.
     * @return              The Sharpe Ratio for that time period.
     */
    public static double calcSharpeRatio (List<Double> returns, DateTime startDate, DateTime endDate) {
        // startDate and endDate required to calculate appropriate risk free rate for different time periods other than yearly.
        int totalNumOfDays = Days.daysBetween(startDate.toLocalDate(), endDate.toLocalDate()).getDays();
        List<Double> returnPercents = calcAllReturns(returns);
        double averageReturnPeriod = totalNumOfDays / returnPercents.size();
        double adjustedRiskFreeRate = Math.pow(1.05, (averageReturnPeriod / 365)) - 1;

        //Calculate necessary statistics
        DescriptiveStatistics calculator = new DescriptiveStatistics();
        for (double percent : returnPercents) {
            calculator.addValue(percent);
        }

        double effectiveAvgReturn = calculator.getMean() - adjustedRiskFreeRate;
        double returnDeviation = calculator.getStandardDeviation();
        double sharpeRatio = effectiveAvgReturn / returnDeviation;

        return sharpeRatio;
    }
}
