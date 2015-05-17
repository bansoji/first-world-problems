import main.History;
import main.Order;
import main.Reader;

public class OrderReaderKoK extends Reader<Order> {

    public OrderReaderKoK(String fileName) {
        parser = new OrderParserKoK(fileName);
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
