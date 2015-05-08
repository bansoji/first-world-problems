package utils;

/**
 * Created by Edwin Li on 8/05/2015.
 */
public class Point {
    private double time; //The x-coordinate, the date
    private double price; // The y-coordinate, the price at that date

    /**
     * Constructor for a Point. requires an X and Y coordinate.
     * @param time      //The x-coordinate.
     * @param price     //The y-coordinate.
     */
    public Point (double time, double price) {
        this.time = time;
        this.price = price;
    }

    /**
     * Returns the x-coordinate, with respect to time (in milliseconds).
     * @return  The x-coordinate (the time in ms).
     */
    public double getX() {
        return time;
    }

    /**
     * Returns the y-coordinate, with respect to price.
     * @return  The y-coordinate (the price).
     */
    public double getY() {
        return price;
    }
}
