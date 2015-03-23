import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.logging.Logger;

/**
 * This class reads an input CSV file and outputs its contents as an ArrayList
 */

public class TransactionReader {
    private CSVReader reader;
    private PriceHistory allPrices;

    //column numbers for input prices data file
    private static final int COMPANY_NAME = 0;
    private static final int DATE = 1;
    private static final int PRICE = 8;

    //column numbers for output order prices data file
    private static final int ORDER_COMPANY_NAME = 0;
    private static final int ORDER_DATE = 1;
    private static final int ORDER_PRICE = 2;
    private static final int ORDER_VOLUME = 3;
    private static final int ORDER_SIGNAL = 5;

    private static final Logger logger = Logger.getLogger("log");

    public TransactionReader(String fileName) {
        try {
            reader = new CSVReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PriceHistory allPrices = new PriceHistory();
    }

    public void readAllPrices() {
        try {
            reader.readNext();     //Skip first line
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String companyName = nextLine[COMPANY_NAME];
                double value;
                Date date = null;

                if (nextLine[PRICE].equals("")) {
                    // no value
                    continue;
                }

                value = Double.parseDouble(nextLine[PRICE]);

                try {
                    DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    date = df.parse(nextLine[DATE]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Price newPrice = new Price(companyName, value, date);
                allPrices.pushPrice(companyName, newPrice);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List getCompanyPrices(String company){
        List companyHistory = allPrices.getCompanyHistory(company);
        return companyHistory;
    }

    public void printCompanyPrices(String company){
        allPrices.printCompanyHistory(company);
    }

    public HashMap<String, ArrayList> getAllPrices(){
        return allPrices.getAllPrices();
    }
}
