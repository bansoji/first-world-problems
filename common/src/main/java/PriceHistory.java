import java.util.*;

/**
 * This class records the price history for all companies.
 */
public class PriceHistory {
    private HashMap<String, List<Price>> allPrices;

    public PriceHistory(){
        this.allPrices = new HashMap<String, List<Price>>();
    }

/*  Given a hashmap company key, this method appends a price to the end of its
*   corresponding ArrayList value.
*/
    public void pushPrice(String company, Price price){
        if (allPrices.containsKey(company)){
            List current = allPrices.get(company);
            current.add(price);
        } else {
            List current = new ArrayList<Price>();
            current.add(price);
            allPrices.put(company, current);
        }
        List current = allPrices.get(company);
        current.add(price);
    }

    public List getCompanyHistory(String company){
        List result = null;
        if (allPrices.containsKey(company)){
            result = allPrices.get(company);
        }
        return result;
    }

    public void printCompanyHistory(String company){
        List<Price> current = allPrices.get(company);
        if (current != null) {
            for (Price price: current) {
                String companyName = price.getCompanyName();
                String companyDate = price.getDate().toString();
                String companyValue = String.valueOf(price.getValue());
                System.out.println(companyName + companyDate + companyValue);
            }
        } else {
            System.out.println("There is no company to print.");
        }
    }

    public HashMap<String, List<Price>> getAllPrices(){
        return allPrices;
    }
}
