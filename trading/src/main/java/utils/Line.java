package utils;

/**
 * Created by jasonlim on 5/05/15.
 */
public class Line {
    private double intercept; // the b in y = mx + b
    private double slope; // the m in y = mx + b

    public Line (double intercept, double slope) {
        this.intercept = intercept;
        this.slope = slope;
    }

    public double getIntercept() {
        return intercept;
    }

    public double getSlope() {
        return slope;
    }
}
