package finance;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class FinanceUtilsTest {

    @Test
    public void testCalcSimpleMovingAverage() {
        List<Double> prices = new ArrayList<>();
        prices.add(30.);
        prices.add(30.34);
        prices.add(30.71);
        //correct to 5 d.p.
        assertEquals(FinanceUtils.calcSimpleMovingAverage(prices,3),0.00784,0.00001);
    }
}