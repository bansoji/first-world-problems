package utils;

/**
 * Created by Jason Lim on 5/05/15.
 * Edited by Edwin Li on 6/05/15.
 */
public class Line {
    private double intercept; // the b in y = mx + b
    private double slope; // the m in y = mx + b

    /**
     * The constructor for a Line.
     * @param intercept     The y-intercept of the line.
     * @param slope         The gradient of the line.
     */
    public Line (double intercept, double slope) {
        this.intercept = intercept;
        this.slope = slope;
    }

    /**
     * Returns the y-intercept of this line.
     * @return The y-intercept.
     */
    public double getIntercept() {
        return intercept;
    }

    /**
     * Returns the gradient of this line.
     * @return The gradient.
     */
    public double getSlope() {
        return slope;
    }
}
