import date.DateUtils;
import core.Order;
import core.OrderType;
import core.Parser;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Order Parser Integration.
 *
 * Used to integrate with another module that uses the format of yyyy-MM-dd.
 * This can be used as a template for other integrations.
 */
public class OrderParserKoK extends Parser<Order> {
    //Column numbers for output order prices data file.
    private static final int ORDER_COMPANY_NAME = 0;
    private static final int ORDER_DATE = 1;
    private static final int ORDER_PRICE = 2;
    private static final int ORDER_VOLUME = 3;
    private static final int ORDER_SIGNAL = 5;

    public OrderParserKoK(String filename){
        super(filename);
    }

    @Override
    public Order parseNextLine() {
        //Read the remaining lines and store data into ArrayList.
        try {
            String[] nextLine = reader.readNext();
            if (nextLine != null) {
                String companyName = nextLine[ORDER_COMPANY_NAME];
                double value;

                //get the next line with a price
                while (nextLine[ORDER_PRICE].equals("")) {
                    nextLine = reader.readNext();       //No value.
                    numberOfFileLines += 1;
                    if (nextLine == null) return null;
                }
                numberOfFileLines += 1;
                value = Double.parseDouble(nextLine[ORDER_PRICE]);
                DateTime date = DateUtils.parseYearFirst(nextLine[ORDER_DATE]);

                OrderType type;
                if (nextLine[ORDER_SIGNAL].equals("B")) {
                    type = OrderType.BUY;
                } else {
                    type = OrderType.SELL;
                }

                int volume = Integer.parseInt(nextLine[ORDER_VOLUME]);
                return new Order(type, companyName, value, volume, date);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
