import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class records the price history for all companies.
 */
public class History<T> {
    private Map<String, List<T>> history;

    public History(){
        this.history = new HashMap<String, List<T>>();
    }

/*  Given a hashmap's company key, this method appends a price to the end of the hashmap's
*   corresponding ArrayList value.
*/
    public void add(String company, T price){
        if (history.containsKey(company)){
            List<T> current = history.get(company);
            current.add(price);
        } else {
            List<T> current = new ArrayList<T>();
            current.add(price);
            history.put(company, current);
        }
    }

    public List<T> getCompanyHistory(String company){
        List<T> result = null;
        if (history.containsKey(company)){
            result = history.get(company);
        }
        return result;
    }

}
