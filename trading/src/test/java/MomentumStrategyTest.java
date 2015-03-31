import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class MomentumStrategyTest {

    @Test
    public void testGenerateOrders() throws Exception {
        String paramName = "trading/resources/config.properties";
        InputStream input = new FileInputStream(paramName);
        Price price00 = new Price("CompanyA", 15.00, new Date(0));
        Price price01 = new Price("CompanyA", 13.25, new Date(1));
        Price price02 = new Price("CompanyA", 16.00, new Date(2));
        Price price03 = new Price("CompanyA", 17.35, new Date(3));
        Price price04 = new Price("CompanyA", 17.35, new Date(4));
        Price price05 = new Price("CompanyA", 9.01, new Date(5));
        Price price06 = new Price("CompanyA", 9.00, new Date(6));
        List<Price> priceList = new ArrayList<>();
        priceList.add(price00);
        priceList.add(price01);
        priceList.add(price02);
        priceList.add(price03);
        priceList.add(price04);
        priceList.add(price05);
        priceList.add(price06);

        TradingStrategy testStrategy = new MomentumStrategy(priceList, input);
        testStrategy.generateOrders();
        List<Order> testOutput = testStrategy.getOrders();
        for (Order i : testOutput)
        {
            System.out.println(i.toStringArray());
        }


    }

    @Test
    public void testGetOrders() throws Exception {

    }

    @Test
    public void testSetMovingAverage() throws Exception {

    }

    @Test
    public void testSetVolume() throws Exception {

    }

    @Test
    public void testSetThreshold() throws Exception {

    }
}