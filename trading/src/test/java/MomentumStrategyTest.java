import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class MomentumStrategyTest {

    @Test
    public void testGenerateOrders() throws Exception {
        String paramName = "resources/config.properties";
        InputStream input = new FileInputStream(paramName);
        SimpleDateFormat testDates = new SimpleDateFormat("dd/MM/yyyy");

        Price price00 = new Price("CompanyA", 8.00, testDates.parse("01/01/2013"));
        Price price01 = new Price("CompanyA", 13.25, testDates.parse("02/01/2013"));
        Price price02 = new Price("CompanyA", 16.00, testDates.parse("03/01/2013"));
        Price price03 = new Price("CompanyA", 17.35, testDates.parse("04/01/2013"));
        Price price04 = new Price("CompanyA", 17.35, testDates.parse("05/01/2013"));
        Price price05 = new Price("CompanyA", 9.01, testDates.parse("06/01/2013"));
        Price price06 = new Price("CompanyA", 9.00, testDates.parse("07/01/2013"));
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
            for (String field : i.toStringArray())
            {
                System.out.println(field);
            }
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