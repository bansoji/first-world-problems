import java.util.List;

/**
 * This class records the order history for all companies.
 */
public class OrderHistory extends History{

    public OrderHistory(){
    }

    public void printCompanyHistory(String company){
        List<Order> current = (List<Order>) getAll().get(company);
        if (current != null) {
            for (Order order: current) {
                String orderCompany = order.getCompanyName();
                String orderPrice = String.valueOf(order.getPrice());
                String orderValue = String.valueOf(order.getValue());
                OrderType orderType = order.getOrderType();
                String orderSignal = orderType.getSignal(orderType);
                String orderDate = order.getOrderDate().toString();
                System.out.println(orderCompany + " " + orderPrice + " " + orderValue + " " + orderSignal + " " + orderDate);
            }
        } else {
            System.out.println("There is no company to print.");
        }
    }

}
