import java.util.*;

/**
 * This class records the price history for all companies.
 */

public class PriceHistory {
    private HashMap<String, List<Price>> allPrices;

    public PriceHistory(){
        this.allPrices = new HashMap<String, List<Price>>();
    }

/*  Given a hashmap's company key, this method appends a price to the end of the hashmap's
*   corresponding ArrayList value.
*/
    public void addPrice(String company, Price price){
        if (allPrices.containsKey(company)){
            List<Price> current = allPrices.get(company);
            current.add(price);
        } else {
            List<Price> current = new ArrayList<Price>();
            current.add(price);
            allPrices.put(company, current);
        }
    }

    public List<Price> getCompanyHistory(String company){
        List<Price> result = null;
        if (allPrices.containsKey(company)){
            result = allPrices.get(company);
        }
        return result;
    }

    public HashMap<String, List<Price>> getAllPrices(){
        return allPrices;
    }

    public void printCompanyHistory(String company){
        List<Price> current = allPrices.get(company);
        if (current != null) {
            for (Price price: current) {
                String companyName = price.getCompanyName();
                String companyDate = price.getDate().toString();
                String companyValue = String.valueOf(price.getValue());
                System.out.println(companyName + " " + companyDate + " " + companyValue);
            }
        } else {
            System.out.println("There is no company to print.");
        }
    }

}
