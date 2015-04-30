import java.util.List;

/**
 * Created by jasonlim on 30/03/15.
 */
public class PriceReader extends Reader<Price> {

    public PriceReader(String fileName) {
        parser = new PriceParser(fileName);
        history = new History<Price>();
    }

    public boolean readAllLines() {
        List<Price> prices = parser.parseAllLines();
        if (prices == null) return false;
        for (Price p: prices) {
            if (p != null) history.add(p.getCompanyName(),p);
        }
        return true;
    }
}
