package core;

import utils.GeometryUtils;
import utils.Line;
import utils.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The profile of a given company.
 */
public class Profile {
    private double averageVolume;
    private double overallTrend;
    private double overallDailyVariance;
    private double dailyDifference;
    private double intraDayVariance;
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
        intraDayVariance = 0.0;

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
                intraDayVariance += Math.abs(p.getValue() - prevDay)/prevDay;
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
        dailyDifference = dailyDifference/ prices.size();
        intraDayVariance = intraDayVariance/ prices.size();

        overallTrend = l.getSlope();
        overallDailyVariance = lHigh.getSlope() - lLow.getSlope();

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

    public double getIntraDayVariance() {
        return intraDayVariance;
    }

    public Map<String,Double> getMetrics() {
        Map<String,Double> metrics = new HashMap<>();
        metrics.put("Average Volume", averageVolume);
        metrics.put("Overall Trend", overallTrend);
        metrics.put("Overall Daily Variance", overallDailyVariance);
        return metrics;
    }

    public String getCompany() {
        return company;
    }

    public static class ProfileEvaluator {
        public static int rate(String metric, double value) {
            if (metric.equals("Average Volume")) {
                return rateVolume(value);
            } else if (metric.equals("Overall Trend")) {
                return rateTrend(value);
            } else if (metric.equals("Overall Daily Variance")) {
                return rateVariance(value);
            } else {
                return -1;
            }
        }

        private static int rateVolume(double value) {
            if (value > 1e9) return 5;
            else if (value > 1e8) return 4;
            else if (value > 1e7) return 3;
            else if (value > 1e6) return 2;
            else return 1;
        }

        private static int rateTrend(double value) {
            if (value > 1) return 5;
            else if (value > 0.75) return 4;
            else if (value > 0.50) return 3;
            else if (value > 0.25) return 2;
            else if (value > 0) return 1;
            else return 0;
        }

        private static int rateVariance(double value) {
            if (value > 1) return 5;
            else if (value > 0.75) return 4;
            else if (value > 0.50) return 3;
            else if (value > 0.25) return 2;
            else if (value > 0) return 1;
            else return 0;
        }
    }
}
