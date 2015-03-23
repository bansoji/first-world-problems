import java.util.*;

/**
 * This class records the price history for all companies
 */
public class PriceHistory {
    private HashMap<String, ArrayList> allPrices;

    public PriceHistory(){
        Map<String, ArrayList> allPrices = new HashMap<String, ArrayList>();
    }

/*  Given a hashmap company key, this method appends a price to the end of its
*   corresponding ArrayList value
*/
    public void pushPrice(String company, Price price){
        List current = allPrices.get(company);
        current.add(price);
    }

    public List getCompanyHistory(String company){
        return allPrices.get(company);
    }

    public void printCompanyHistory(String company){
        List<Price> current = allPrices.get(company);
        for (Price price: current){
            String companyName = price.getCompanyName();
            String companyDate = price.getDate().toString();
            String companyValue = String.valueOf(price.getValue());
            System.out.println(companyName + companyDate + companyValue);
        }
    }
}
