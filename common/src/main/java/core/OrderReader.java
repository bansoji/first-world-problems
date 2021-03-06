package core;

/**
 * This class reads an input CSV file and outputs its contents as an ArrayList.
 */

public class OrderReader extends Reader<Order> {

    public OrderReader(String fileName) {
        parser = new OrderParser(fileName);
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
