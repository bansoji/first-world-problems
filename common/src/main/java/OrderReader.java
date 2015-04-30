import java.util.List;

/**
 * This class reads an input CSV file and outputs its contents as an ArrayList.
 */

public class OrderReader extends Reader<Order> {

    public OrderReader(String fileName) {
        parser = new OrderParser(fileName);
        history = new History<Order>();
    }

    public boolean readAllLines() {
        List<Order> orders = parser.parseAllLines();
        if (orders == null) return false;
        for (Order o: orders) {
            if (o != null) history.add(o.getCompanyName(),o);
        }
        return true;
    }
}
