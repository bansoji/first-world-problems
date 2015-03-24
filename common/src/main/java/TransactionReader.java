import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.logging.Logger;

/**
 * This class reads an input CSV file and outputs its contents as an ArrayList
 */

public class TransactionReader {
    private CSVReader reader;

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
            logger.warning("Input file cannot be opened.");
        }
    }

    public List<Price> getAllPrices() {
        List<Price> allPrices = new ArrayList<Price>();

        //Read the remaining lines and store data into ArrayList
        try {
            //Skip first line
            reader.readNext();
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
                allPrices.add(newPrice);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Cannot find the reader. " + e);
        }
        return allPrices;
    }

    public List<Order> getAllOrders() {
        List<Order> allOrders = new ArrayList<Order>();

        //Read the remaining lines and store data into ArrayList
        try {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String companyName = nextLine[ORDER_COMPANY_NAME];
                double value;
                Date date = null;

                if (nextLine[ORDER_PRICE].equals("")) {
                    // no value
                    continue;
                }
                value = Double.parseDouble(nextLine[ORDER_PRICE]);

                try {
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    date = df.parse(nextLine[ORDER_DATE]);
                } catch (ParseException e) {
                    e.printStackTrace();
                    logger.severe("Incorrect date format in the input file.");
                }

                OrderType type;
                if (nextLine[ORDER_SIGNAL].equals("B")) {
                    type = OrderType.BUY;
                } else {
                    type = OrderType.SELL;
                }

                int volume = Integer.parseInt(nextLine[ORDER_VOLUME]);

                Order order = new Order(type,companyName, value, volume, date);
                allOrders.add(order);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allOrders;
    }
}
