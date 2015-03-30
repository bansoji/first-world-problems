import java.util.List;

/**
 * Created by jasonlim on 30/03/15.
 */
public abstract class Reader<T> {
    protected Parser parser;
    protected History<T> history;

    public List<T> getCompanyHistory(String companyName)
    {
        return history.getCompanyHistory(companyName);
    }

    protected abstract boolean readNext();
    public void readAll()
    {
        while (readNext());
        parser.close();
    }

    public History<T> getHistory() {
        return history;
    }
}
