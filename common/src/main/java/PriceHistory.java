import java.util.*;

/**
 * This class records the price history for all companies.
 */

public class PriceHistory extends History {
    public PriceHistory() {
    }

    public void printCompanyHistory(String company){
        List<Price> current = (List<Price>) getAll().get(company);
        if (current != null) {
            for (Price price: current) {
                String priceCompany = price.getCompanyName();
                String priceDate = price.getDate().toString();
                String priceValue = String.valueOf(price.getValue());
                System.out.println(priceCompany + " " + priceDate + " " + priceValue);
            }
        } else {
            System.out.println("There is no company to print.");
        }
    }

}
