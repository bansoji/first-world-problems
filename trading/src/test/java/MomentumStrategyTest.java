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


        /*Price price09 = new Price("CompanyA", 10, testDates.parse("01/01/2013"));
        Price price10 = new Price("CompanyA", 10.1, testDates.parse("02/01/2013"));
        Price price11 = new Price("CompanyA", 10.3, testDates.parse("03/01/2013"));
        Price price12 = new Price("CompanyA", 10.7, testDates.parse("04/01/2013"));
        Price price13 = new Price("CompanyA", 11.2, testDates.parse("05/01/2013"));
        Price price00 = new Price("CompanyA", 11.2, testDates.parse("06/01/2013"));
        Price price01 = new Price("CompanyA", 11.19, testDates.parse("07/01/2013"));
        Price price02 = new Price("CompanyA", 11.15, testDates.parse("08/01/2013"));
        Price price03 = new Price("CompanyA", 10.8, testDates.parse("09/01/2013"));
        Price price04 = new Price("CompanyA", 10.5, testDates.parse("10/01/2013"));
        Price price05 = new Price("CompanyA", 10.6, testDates.parse("11/01/2013"));
        Price price06 = new Price("CompanyA", 10.5, testDates.parse("12/01/2013"));
        Price price07 = new Price("CompanyA", 9.05, testDates.parse("13/01/2013"));
        Price price08 = new Price("CompanyA", 13.1, testDates.parse("14/01/2013"));*/

        Price price01 = new Price("CompanyA", 30, testDates.parse("01/01/2013"));
        Price price02 = new Price("CompanyA", 30.34, testDates.parse("02/01/2013"));
        Price price03 = new Price("CompanyA", 30.71, testDates.parse("03/01/2013"));
        Price price04 = new Price("CompanyA", 31.09, testDates.parse("04/01/2013"));
        Price price05 = new Price("CompanyA", 31.29, testDates.parse("05/01/2013"));
        Price price06 = new Price("CompanyA", 31.54, testDates.parse("06/01/2013"));
        Price price07 = new Price("CompanyA", 31.76, testDates.parse("07/01/2013"));
        Price price08 = new Price("CompanyA", 31.98, testDates.parse("08/01/2013"));
        Price price09 = new Price("CompanyA", 32.58, testDates.parse("09/01/2013"));


        List<Price> priceList = new ArrayList<>();
        /*priceList.add(price09);
        priceList.add(price10);
        priceList.add(price11);
        priceList.add(price12);
        priceList.add(price13);
        priceList.add(price00);
        */
        priceList.add(price01);
        priceList.add(price02);
        priceList.add(price03);
        priceList.add(price04);
        priceList.add(price05);
        priceList.add(price06);
        priceList.add(price07);
        priceList.add(price08);
        priceList.add(price09);


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