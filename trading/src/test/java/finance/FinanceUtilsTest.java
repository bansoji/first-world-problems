package finance;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FinanceUtilsTest {

    @Test
    public void testCalcSimpleMovingAverage() {
        List<Double> prices = new ArrayList<Double>();
        prices.add(30.);
        prices.add(30.34);
        prices.add(30.71);
        List<Double> simpleMovingAverages = FinanceUtils.calcAllSimpleMovingAvg(prices, 3);
        //correct to 5 d.p.
        assertEquals(simpleMovingAverages.get(0), 0.00784, 0.00001);

        prices.add(31.09);
        simpleMovingAverages = FinanceUtils.calcAllSimpleMovingAvg(prices, 3);
        assertEquals(simpleMovingAverages.get(0), 0.00784, 0.00001);
        assertEquals(simpleMovingAverages.get(1), 0.01197, 0.00001);
    }
}