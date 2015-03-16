import java.io.IOException;
import java.util.ArrayList;

/**
 * Main class
 */

public class OrderManager {
    public static void main(String[] args) throws IOException {
        String fileName = "/Users/jasonlim/Desktop/Projects/first-world-problems/trading/resources/sampleData";

        TransactionReader tReader = new TransactionReader(fileName);
        ArrayList<Price> allPrices = tReader.getAllPrices();
        ArrayList<String> columnContents = tReader.getColumnContents(1);

        //prints allPrices
        /*
        for (Price price: allPrices){
            System.out.print(price.getCompanyName() + " ");
            System.out.print(price.getValue() + " ");
            System.out.print(price.getDate() + "\n");
        }
        */

        //prints columnContents
        /*
        for (String line:columnContents){
            System.out.println(line);
        }
        */
    }

}
