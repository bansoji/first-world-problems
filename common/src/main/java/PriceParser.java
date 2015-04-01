import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jasonlim on 30/03/15.
 */
public class PriceParser extends Parser<Price> {

    //Column numbers for input prices data file.
    private static final int COMPANY_NAME = 0;
    private static final int DATE = 1;
    private static final int PRICE = 8;

    public PriceParser(String filename){
        super(filename);
    }

    @Override
    public Price parseNextLine() {
        try {
            String[] nextLine = reader.readNext();
            if (nextLine != null) {
                String companyName = nextLine[COMPANY_NAME];
                double value;
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

                try {
                    DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    date = df.parse(nextLine[DATE]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return new Price(companyName, value, date);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Cannot find the reader. " + e);
        }
        return null;
    }

}
