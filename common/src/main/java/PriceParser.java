import date.DateUtils;

import java.io.IOException;
import java.util.Date;

/**
 * Created by jasonlim on 30/03/15.
 */
public class PriceParser extends Parser<Price> {

    //Column numbers for input prices data file.
    private static final int COMPANY_NAME = 0;
    private static final int DATE = 1;
    private static final int PRICE = 8;
    private static final int OPEN_PRICE = 5;
    private static final int HIGH_PRICE = 6;
    private static final int LOW_PRICE = 7;
    private static final int VOLUME = 9;

    public PriceParser(String filename){
        super(filename);
    }

    @Override
    public Price parseNextLine() {
        try {
            String[] nextLine = reader.readNext();
            if (nextLine != null) {
                String companyName = nextLine[COMPANY_NAME];
                double value, open_price, high_price, low_price;
                int volume;
                Date date = null;

                //get the next line with a price
                while (nextLine[PRICE].equals("")) {
                    numberOfFileLines += 1;
                    nextLine = reader.readNext();
                    if (nextLine.length < PRICE) {
                        for (int i = 0; i < nextLine.length; i++)
                        {
                            System.out.println(nextLine[i]);
                        }
                    }
                    if (nextLine == null) return null;
                }
                numberOfFileLines += 1;


                value = Double.parseDouble(nextLine[PRICE]);
                date = DateUtils.parseMonthAbbr(nextLine[DATE], "Error parsing price date");
                open_price = Double.parseDouble(nextLine[OPEN_PRICE]);
                high_price = Double.parseDouble(nextLine[HIGH_PRICE]);
                low_price = Double.parseDouble(nextLine[LOW_PRICE]);
                volume = Integer.parseInt(nextLine[VOLUME]);

                return new Price(companyName, value, date, open_price, high_price, low_price, volume);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Cannot find the reader. " + e);
        }
        return null;
    }

}
