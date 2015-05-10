package utils;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.List;

/**
 * Created by Jason Lim on 5/05/15.
 * Edited by Edwin Li on 6/05/15.
 */
public class GeometryUtils {

    /**
     * This will create a line of best fit from a given set of points.
     * @param points    The set of points.
     * @return          The Slope and Intercept of a Line.
     */
    public static Line createLine(List<Point> points) {
        SimpleRegression lineMaker = new SimpleRegression();
        for (Point point : points) {
            lineMaker.addData(point.getX(), point.getY());
        }
        Line result = new Line(lineMaker.getIntercept(), lineMaker.getSlope());
        return result;
    }

    /**
     * This method will attempt to re-construct the line of best fit with a fixed gradient.
     * @param slope     The given slope to work with.
     * @param points    The set of observations.
     * @return          The new line of best fit.
     */
    public static Line normaliseLine (double slope, List<Point> points) {
        Point average = findAverage(points);
        double intercept = calculateIntercept(slope, average);
        Line result = new Line(intercept, slope);

        return result;
    }

    /**
     * Finds the average observation given a set of points.
     * @param points    The set of points.
     * @return          The average point.
     */
    private static Point findAverage (List<Point> points) {
        double totalPrice = 0.00;
        double totalTime = 0.00;
        for (Point value : points) {
            totalTime += value.getX();
            totalPrice += value.getY();
        }
        double averageTime = totalTime / points.size();
        double averagePrice = totalPrice / points.size();
        Point averagePoint = new Point(averageTime, averagePrice);

        return averagePoint;
    }

    /**
     * Finds the y-intercept of a line given the x and y coordinates and the slope according to the equation:
     * b = y - mx
     * @param slope     The given slope (m).
     * @param point     The given point (x and y coordinates).
     * @return          The y-intercept.
     */
    private static double calculateIntercept (double slope, Point point) {
        //y = mx + b
        // b = y - mx
        return point.getY() - (slope * point.getX());
    }

    /**
     * Given a line, extrapolates a predicted price from a given date.
     * @param line  The line to extrapolate from.
     * @param time  The given date (in milliseconds).
     * @return      The predicted price.
     */
    public static double predictedPrice (Line line, double time) {
        //y = mx + b
        return (line.getSlope() * time) + line.getIntercept();
    }
}
