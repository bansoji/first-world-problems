package core;

import utils.GeometryUtils;
import utils.Line;
import utils.Point;

import java.util.*;

/**
 * The profile of a given company.
 */
public class Profile {
    private double averageVolume;
    private double overallTrend;
    private double overallDailyVariance;
    private double dailyDifference;
    private double interDayVariance;
    private String company;

    /**
     * Build the profile for a company.
     * @param prices The prices for a given company.
     */
    public Profile(List<Price> prices){

        if (prices == null || prices.size() == 0) return;
        company = prices.get(0).getCompanyName();

        averageVolume = 0.0;
        averageVolume = 0.0;
        dailyDifference = 0.0;
        interDayVariance = 0.0;

        List<Point> endOfDayPoints = new ArrayList<>();
        List<Point> highPoints = new ArrayList<>();
        List<Point> lowPoints= new ArrayList<>();

        int i = 0;
        double prevDay = 0.0;

        // Build data.
        for (Price p : prices){
            averageVolume += p.getVolume();
            dailyDifference += Math.abs(p.getValue() - p.getOpen())/p.getValue();

            Point pt = new Point(i, p.getValue());
            Point ptLow = new Point(i, p.getHigh());
            Point ptHigh = new Point(i, p.getLow());

            if (i == 0){
                prevDay = p.getValue();
            } else {
                interDayVariance += Math.abs(p.getValue() - prevDay)/prevDay;
                prevDay = p.getValue();
            }

            endOfDayPoints.add(pt);
            highPoints.add(ptHigh);
            lowPoints.add(ptLow);

            i += 1;
        }

        Line l = GeometryUtils.createLine(endOfDayPoints);
        Line lHigh = GeometryUtils.createLine(highPoints);
        Line lLow = GeometryUtils.createLine(lowPoints);

        averageVolume = averageVolume/ prices.size();
        dailyDifference = dailyDifference * 100.0/ prices.size();
        interDayVariance = interDayVariance*100.0 / prices.size();

        overallTrend = l.getSlope()*1000000000;
        overallDailyVariance = lHigh.getSlope() - lLow.getSlope() * 100.0;

    }

    public double getAverageVolume(){
        return averageVolume;
    }

    public double getOverallTrend() {
        return overallTrend;
    }

    public double getOverallDailyVariance() {
        return overallDailyVariance;
    }

    public double getDailyDifference() {
        return dailyDifference;
    }

    public double getInterDayVariance() {
        return interDayVariance;
    }

    public Map<String,Integer> getRatedMetrics() {
        Map<String,Integer> metrics = new LinkedHashMap<>();
        metrics.put("Volume", ProfileEvaluator.rate("Volume",averageVolume));
        metrics.put("Trend", ProfileEvaluator.rate("Trend",overallTrend));
        metrics.put("Daily Variance", ProfileEvaluator.rate("Daily Variance", overallDailyVariance));
        metrics.put("Daily Difference", ProfileEvaluator.rate("Daily Difference", dailyDifference));
        metrics.put("Inter-day Variance", ProfileEvaluator.rate("Inter-day Variance", interDayVariance));
        return metrics;
    }

    public Map<String,Double> getMetrics() {
        Map<String,Double> metrics = new LinkedHashMap<>();
        metrics.put("Volume", averageVolume);
        metrics.put("Trend", overallTrend);
        metrics.put("Daily Variance", overallDailyVariance);
        metrics.put("Daily Difference", dailyDifference);
        metrics.put("Inter-day Variance", interDayVariance);
        return metrics;
    }

    public String getCompany() {
        return company;
    }

    public static class ProfileEvaluator {
        public static int rate(String metric, double value) {
            if (metric.equals("Volume")) {
                return rateVolume(value);
            } else if (metric.equals("Trend")) {
                return rateTrend(value);
            } else if (metric.equals("Daily Variance")) {
                return rateVariance(value);
            } else if (metric.equals("Daily Difference")) {
                return rateDailyDifference(value);
            } else if (metric.equals("Inter-day Variance")) {
                return rateIntraDayVariance(value);
            } else {
                return -1;
            }
        }

        private static int rateVolume(double value) {
            if (value > 1e6) return 5;
            else if (value > 1e5) return 4;
            else if (value > 1e4) return 3;
            else if (value > 1e3) return 2;
            else return 1;
        }

        private static int rateTrend(double value) {
            if (value > 0.4) return 5;
            else if (value > 0.2) return 4;
            else if (value > 0.15) return 3;
            else if (value > 0.1) return 2;
            else return 1;
        }

        private static int rateVariance(double value) {
            if (value > 1) return 5;
            else if (value > 0.75) return 4;
            else if (value > 0.50) return 3;
            else if (value > 0.25) return 2;
            else return 1;
        }

        private static int rateDailyDifference(double value) {
            if (value > 10) return 5;
            else if (value > 5) return 4;
            else if (value > 1) return 3;
            else if (value > 0.1) return 2;
            else return 1;
        }

        private static int rateIntraDayVariance(double value) {
            if (value > 10) return 5;
            else if (value > 5) return 4;
            else if (value > 1) return 3;
            else if (value > 0.1) return 2;
            else return 1;
        }
    }
}
