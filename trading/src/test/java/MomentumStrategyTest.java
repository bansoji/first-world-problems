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
        Price price07 = new Price("CompanyA", 9.05, testDates.parse("08/01/2013"));
        Price price08 = new Price("CompanyA", 9.06, testDates.parse("09/01/2013"));
        Price price09 = new Price("CompanyA", 12.13, testDates.parse("10/01/2013"));
        Price price10 = new Price("CompanyA", 14.15, testDates.parse("11/01/2013"));
        Price price11 = new Price("CompanyA", 17.00, testDates.parse("12/01/2013"));
        Price price12 = new Price("CompanyA", 16.59, testDates.parse("13/01/2013"));
        Price price13 = new Price("CompanyA", 14.27, testDates.parse("14/01/2013"));

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
        assert(testOutput.size() != 0);
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