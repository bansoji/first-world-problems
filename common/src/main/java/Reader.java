import java.util.List;
import java.util.logging.Logger;

/**
 * Created by jasonlim on 30/03/15.
 */
public abstract class Reader<T> {
    protected Parser parser;
    protected History<T> history;

    private static final Logger logger = Logger.getLogger("log");

    public List<T> getCompanyHistory(String companyName)
    {
        return history.getCompanyHistory(companyName);
    }

    protected abstract boolean readNext();
    public void readAll()
    {
        while (readNext());
        parser.close();
        logger.info(String.valueOf("Number of file lines read is " + parser.getNumberOfFileLines()));
    }

    public History<T> getHistory() {
        return history;
    }

}
