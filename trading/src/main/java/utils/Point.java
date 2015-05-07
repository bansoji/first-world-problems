package utils;

/**
 * Created by Edwin Li on 8/05/2015.
 */
public class Point {
    private double time; //The x-coordinate, the date
    private double price; // The y-coordinate, the price at that date

    public Point (double time, double price) {
        this.time = time;
        this.price = price;
    }

    public double getX() {
        return time;
    }

    public double getY() {
        return price;
    }
}
