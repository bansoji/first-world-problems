import core.History;
import core.Order;
import core.Reader;

/**
 * This class reads an input CSV file and outputs its contents as an ArrayList.
 */

public class OrderReaderWolf extends Reader<Order> {

    public OrderReaderWolf(String fileName) {
        parser = new OrderParserWolf(fileName);
        history = new History<Order>();
    }

    protected boolean readNext()
    {
        Order nextOrder = (Order)parser.parseNextLine();
        if (nextOrder == null) return false;
        history.add(nextOrder.getCompanyName(),nextOrder);
        return true;
    }
}
