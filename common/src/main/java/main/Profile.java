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
    private double dailyDifference;

    /**
     * Build the profile for a company.
     * @param prices The prices for a given company.
     */
    public Profile(ArrayList<Price> prices){

        averageVolume = 0.0;
        dailyDifference = 0.0;
        ArrayList<Point> endOfDayPoints = new ArrayList<>();
        ArrayList<Point> highPoints = new ArrayList<>();
        ArrayList<Point> lowPoints= new ArrayList<>();

        double i = 0.0;

        // Build data.
        for (Price p : prices){
            averageVolume += p.getVolume();
            dailyDifference += Math.abs(p.getValue() - p.getOpen())/p.getValue();

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
        dailyDifference = dailyDifference/ prices.size();

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
}
