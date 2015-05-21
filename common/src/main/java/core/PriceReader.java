package core;

/**
 * Created by jasonlim on 30/03/15.
 */
public class PriceReader extends Reader<Price> {

    public PriceReader(String fileName) {
        parser = new PriceParser(fileName);
        history = new History<Price>();
    }

    protected boolean readNext()
    {
        Price nextPrice = (Price)parser.parseNextLine();
        if (nextPrice == null) return false;
        history.add(nextPrice.getCompanyName(),nextPrice);
        return true;
    }
}
