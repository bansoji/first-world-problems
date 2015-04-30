import date.DateUtils;

import java.util.List;

/**
 * Created by jasonlim on 30/03/15.
 */
public class PrintUtils {

    public static void printOrders(List<Order> orders)
    {
        if (orders.size() == 0) {
            System.out.println("There are no orders to print.");
            return;
        }
        for (Order order: orders) {
            String orderCompany = order.getCompanyName();
            double orderPrice = order.getPrice();
            double orderValue = order.getValue();
            OrderType orderType = order.getOrderType();
            String orderSignal = orderType.getSignal(orderType);
            String orderDate = order.getOrderDate();
            System.out.println(orderCompany + " " + orderPrice + " " + orderValue + " " + orderSignal + " " + orderDate);
        }
    }

    public static void printPrices(List<Price> prices)
    {
        if (prices.size() == 0) {
            System.out.println("There is no prices to print.");
            return;
        }
        for (Price price: prices) {
            String priceCompany = price.getCompanyName();
            String priceDate = price.getDate();
            double priceValue = price.getValue();
            System.out.println(priceCompany + " " + priceDate + " " + priceValue);
        }
    }
}
