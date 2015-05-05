package finance;

import org.junit.Test;
import utils.FinanceUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FinanceUtilsTest {

    @Test
    public void testCalcSimpleMovingAvg() {
        List<Double> prices = new ArrayList<Double>();
        prices.add(30.);
        prices.add(30.34);
        prices.add(30.71);
        prices.add(31.09);
        List<Double> simpleMovingAverages = FinanceUtils.calcAllSimpleMovingAvg(prices, 3);
        //correct to 5 d.p.
        System.out.println("Testing all SMA calculations for over 3 day window:");
        for (double price: prices) {
            System.out.print(price + " ");
        }
        System.out.println();
        assertEquals(simpleMovingAverages.get(0), 0.01197, 0.00001);
        prices.add(31.29);
        System.out.println("Added 31.29 to price list");
        simpleMovingAverages = FinanceUtils.calcAllSimpleMovingAvg(prices, 3);
        assertEquals(simpleMovingAverages.get(0), 0.01197, 0.00001);
        assertEquals(simpleMovingAverages.get(1), 0.01033, 0.00001);
    }

}