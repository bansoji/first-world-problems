package stock;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by gavintam on 29/05/15.
 */
public class YahooFinance {
    private static Logger logger = Logger.getLogger("application_log");

    public static List<Stock> get(String[] symbols) {
        List<Stock> stocks = new ArrayList<>();
        try {
            StringBuilder sb = new StringBuilder();
            for (String n : symbols) {
                if (sb.length() > 0) sb.append(',');
                sb.append("'").append(n).append("'");
            }
            String symbolList = sb.toString();
            URL yahoo = new URL("http://finance.yahoo.com/d/quotes.csv?s="+ symbolList + "&f=nsl1p2");
            URLConnection connection = yahoo.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            CSVReader csvReader = new CSVReader(br);
            List<String[]> values = csvReader.readAll();
            csvReader.close();
            for (String[] value: values) {
                value[3] = value[3].replace("%", "");
                Stock stock = new Stock(value[0], value[1], value[2], value[3]);
                stocks.add(stock);
            }

        } catch (Exception e) {
            logger.severe(e.getMessage());
            return null;
        }
        return stocks;
    }

    public static Stock get(String symbol) {
        Stock stock = null;
        try {
            URL yahoo = new URL("http://finance.yahoo.com/d/quotes.csv?s="+ symbol + "&f=nl1p2");
            URLConnection connection = yahoo.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            CSVReader csvReader = new CSVReader(br);
            String[] values = csvReader.readNext();
            csvReader.close();
            values[2] = values[2].replace("%","");
            stock = new Stock(values[0],symbol,values[1],values[2]);

        } catch (Exception e) {
            logger.severe(e.getMessage());
            return null;
        }
        return stock;
    }
}
