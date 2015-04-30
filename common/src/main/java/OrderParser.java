import date.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jasonlim on 30/03/15.
 */
public class OrderParser extends Parser<Order> {

    //Column numbers for output order prices data file.
    private static final int ORDER_COMPANY_NAME = 0;
    private static final int ORDER_DATE = 1;
    private static final int ORDER_PRICE = 2;
    private static final int ORDER_VOLUME = 3;
    private static final int ORDER_SIGNAL = 5;

    public OrderParser(String filename){
        super(filename);
    }

    @Override
    public List<Order> parseAllLines() {
        //Read the remaining lines and store data into ArrayList.
        try {
            List<String[]> lines = reader.readAll();
            List<Order> orders = new ArrayList<>();
            for (String[] nextLine: lines) {
                if (nextLine != null) {
                    String companyName = nextLine[ORDER_COMPANY_NAME];
                    double value;
                    Date date = null;

                    //get the next line with a price
                    if (nextLine[ORDER_PRICE].equals("")) {
                        numberOfFileLines += 1;
                        continue;
                    }
                    numberOfFileLines += 1;
                    value = Double.parseDouble(nextLine[ORDER_PRICE]);
                    date = DateUtils.parse(nextLine[ORDER_DATE], "Incorrect date format in the input file.");

                    OrderType type;
                    if (nextLine[ORDER_SIGNAL].equals("B")) {
                        type = OrderType.BUY;
                    } else {
                        type = OrderType.SELL;
                    }

                    int volume = Integer.parseInt(nextLine[ORDER_VOLUME]);
                    orders.add(new Order(type, companyName, value, volume, date));
                }
                return orders;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
