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

    /**
     * Build the profile for a company.
     * @param prices The prices for a given company.
     */
    public Profile(List<Price> prices){

        averageVolume = 0.0;
        List<Point> endOfDayPoints = new ArrayList<>();
        List<Point> highPoints = new ArrayList<>();
        List<Point> lowPoints= new ArrayList<>();

        double i = 0.0;

        // Build data.
        for (Price p : prices){
            averageVolume += p.getVolume();

            Point pt = new Point(i, p.getValue());
            Point ptLow = new Point(i, p.getHigh());
            Point ptHigh = new Point(i, p.getLow());

            endOfDayPoints.add(pt);
            highPoints.add(ptHigh);
            lowPoints.add(ptLow);

            i += 1.0;
        }

        Line l = GeometryUtils.createLine(endOfDayPoints);
        Line lHigh = GeometryUtils.createLine(highPoints);
        Line lLow = GeometryUtils.createLine(lowPoints);

        averageVolume = averageVolume/ prices.size();

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

    public Map<String,Double> getMetrics() {
        Map<String,Double> metrics = new HashMap<>();
        metrics.put("Average Volume", averageVolume);
        metrics.put("Overall Trend", overallTrend);
        metrics.put("Overall Daily Variance", overallDailyVariance);
        return metrics;
    }

    private static class ProfileEvaluator {
        private int rateVolume(double value) {
            if (value > 1e9) return 5;
            else if (value > 1e8) return 4;
            else if (value > 1e7) return 3;
            else if (value > 1e6) return 2;
            else return 1;
        }

        private int rateTrend(double value) {
            if (value > 1) return 5;
            else if (value > 0.75) return 4;
            else if (value > 0.50) return 3;
            else if (value > 0.25) return 2;
            else if (value > 0) return 1;
            else if (value == 0) return 0;
            else if (value > -0.25) return -1;
            else if (value > -0.50) return -2;
            else if (value > -0.75) return -3;
            else if (value > -1.0) return -4;
            else return -5;
        }
    }
}
