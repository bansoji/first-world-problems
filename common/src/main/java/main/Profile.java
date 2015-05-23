package main;

import utils.GeometryUtils;
import utils.Line;
import utils.Point;

import java.util.ArrayList;

/**
 * The profile of a given company.
 */
public class Profile {
    private double averageVolume;
    private double overallTrend;
    private double overallDailyVariance;
     /* The price difference within the days.
    Calculated using the % difference of the open and close prices. */
    private double dailyDifference;
    private double intraDayVariance; /* The price difference between the days. */

    /**
     * Build the profile for a company.
     * @param prices The prices for a given company.
     */
    public Profile(ArrayList<Price> prices){

        averageVolume = 0.0;
        dailyDifference = 0.0;
        intraDayVariance = 0.0;

        ArrayList<Point> endOfDayPoints = new ArrayList<>();
        ArrayList<Point> highPoints = new ArrayList<>();
        ArrayList<Point> lowPoints= new ArrayList<>();

        int i = 0;
        double prevDay = 0;

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
}
