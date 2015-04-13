import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    public Order parseNextLine() {
        //Read the remaining lines and store data into ArrayList.
        try {
            String[] nextLine = reader.readNext();
            if (nextLine != null) {
                String companyName = nextLine[ORDER_COMPANY_NAME];
                double value;
                Date date = null;

                //get the next line with a price
                while (nextLine[ORDER_PRICE].equals("")) {
                    nextLine = reader.readNext();       //No value.
                    numberOfFileLines += 1;
                    if (nextLine == null) return null;
                }
                numberOfFileLines += 1;
                value = Double.parseDouble(nextLine[ORDER_PRICE]);
                date = DateUtils.parse(nextLine[ORDER_DATE],"Incorrect date format in the input file.");

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
