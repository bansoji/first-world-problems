//import org.junit.Test;
//
//import java.io.FileInputStream;
//import java.io.InputStream;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
//public class MomentumStrategyTest {
//
//    @Test
//    public void testGenerateOrders() throws Exception {
//        try {
//            test1();
//            test2();
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    private void test1() throws Exception
//    {
//        System.out.println("Testing with:");
//        String paramName = "trading/src/test/resources/configTest.properties";
//        InputStream input = new FileInputStream(paramName);
//
//        Price price01 = new Price("CompanyA", 10, "01/01/2013", 0, 0, 0, 0);
//        Price price02 = new Price("CompanyA", 10.1, "02/01/2013", 0, 0, 0, 0);
//        Price price03 = new Price("CompanyA", 10.3, "03/01/2013", 0, 0, 0, 0);
//        Price price04 = new Price("CompanyA", 10.7, "04/01/2013", 0, 0, 0, 0);
//        Price price05 = new Price("CompanyA", 11.2, "05/01/2013", 0, 0, 0, 0);
//        Price price06 = new Price("CompanyA", 11.2, "06/01/2013", 0, 0, 0, 0);
//        Price price07 = new Price("CompanyA", 11.19, "07/01/2013", 0, 0, 0, 0);
//        Price price08 = new Price("CompanyA", 11.15, "08/01/2013", 0, 0, 0, 0);
//        Price price09 = new Price("CompanyA", 10.8, "09/01/2013", 0, 0, 0, 0);
//        Price price10 = new Price("CompanyA", 10.5, "10/01/2013", 0, 0, 0, 0);
//        Price price11 = new Price("CompanyA", 10.6, "11/01/2013", 0, 0, 0, 0);
//        Price price12 = new Price("CompanyA", 10.5, "12/01/2013", 0, 0, 0, 0);
//        Price price13 = new Price("CompanyA", 9.05, "13/01/2013", 0, 0, 0, 0);
//        Price price14 = new Price("CompanyA", 13.1, "14/01/2013", 0, 0, 0, 0);
//
//        List<Price> priceList = new ArrayList<>();
//        priceList.add(price01);
//        priceList.add(price02);
//        priceList.add(price03);
//        priceList.add(price04);
//        priceList.add(price05);
//        priceList.add(price06);
//        priceList.add(price07);
//        priceList.add(price09);
//        priceList.add(price10);
//        priceList.add(price11);
//        priceList.add(price12);
//        priceList.add(price13);
//        priceList.add(price14);
//
//        for (Price price: priceList)
//        {
//            System.out.println(price.getDate() + ": " + price.getValue());
//        }
//
//        TradingStrategy testStrategy = new MomentumStrategy(priceList, input);
//        testStrategy.generateOrders();
//        List<Order> testOutput = testStrategy.getOrders();
//        assert(testOutput.size() == 3);
//
//        assertEquals(testOutput.get(0).getPrice(), 10.6, 0.0001 );
//        assertEquals(testOutput.get(1).getPrice(), 10.5, 0.0001);
//        assertEquals(testOutput.get(2).getPrice(), 13.1, 0.0001 );
//    }
//
//    private void test2() throws Exception
//    {
//        System.out.println("Testing with:");
//        String paramName = "trading/src/test/resources/configTest.properties";
//        InputStream input = new FileInputStream(paramName);
//
//        Price price01 = new Price("CompanyA", 30, "01/01/2013", 0, 0, 0, 0);
//        Price price02 = new Price("CompanyA", 30.34, "02/01/2013", 0, 0, 0, 0);
//        Price price03 = new Price("CompanyA", 30.71, "03/01/2013", 0, 0, 0, 0);
//        Price price04 = new Price("CompanyA", 31.09, "04/01/2013", 0, 0, 0, 0);
//        Price price05 = new Price("CompanyA", 31.29, "05/01/2013", 0, 0, 0, 0);
//        Price price06 = new Price("CompanyA", 31.54, "06/01/2013", 0, 0, 0, 0);
//        Price price07 = new Price("CompanyA", 31.76, "07/01/2013", 0, 0, 0, 0);
//        Price price08 = new Price("CompanyA", 31.98, "08/01/2013", 0, 0, 0, 0);
//        Price price09 = new Price("CompanyA", 32.58, "09/01/2013", 0, 0, 0, 0);
//
//        List<Price> priceList = new ArrayList<>();
//        priceList.add(price01);
//        priceList.add(price02);
//        priceList.add(price03);
//        priceList.add(price04);
//        priceList.add(price05);
//        priceList.add(price06);
//        priceList.add(price07);
//        priceList.add(price08);
//        priceList.add(price09);
//
//        for (Price price: priceList)
//        {
//            System.out.println(price.getDate() + ": " + price.getValue());
//        }
//
//        TradingStrategy testStrategy = new MomentumStrategy(priceList, input);
//        testStrategy.generateOrders();
//        List<Order> testOutput = testStrategy.getOrders();
//        assert(testOutput.size() == 1);
//
//        assertEquals(testOutput.get(0).getPrice(), 32.58, 0.0001 );
//    }
//}