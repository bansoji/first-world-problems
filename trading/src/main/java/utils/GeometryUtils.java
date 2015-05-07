package utils;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.List;

/**
 * Created by jasonlim on 5/05/15.
 */
public class GeometryUtils {

    public static Line createLine(List<Double> values) {
        SimpleRegression lineMaker = new SimpleRegression();
        for (double point : values) {
            lineMaker.addData(point); // WILL NEED TO FIX THIS
        }
        Line result = new Line(lineMaker.getIntercept(), lineMaker.getSlope());
        return result;
    }
}
