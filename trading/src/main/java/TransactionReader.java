import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;



/**
 * This class reads an input CSV file and outputs its contents as an ArrayList
 */

public class TransactionReader {
    private CSVReader reader;

    public TransactionReader(String fileName){
        try {
            reader = new CSVReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Price> getAllPrices(){
        String[] nextLine;
        ArrayList<Price> allPrices = new ArrayList<Price>();

        //Read the first line of the file (header file)
        int i = 0;
        try {
            while ((reader.readNext())!= null && i < 1){
                i += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Read the remaining lines and store data into ArrayList
        try {
            while ((nextLine = reader.readNext()) != null){
                String companyName = nextLine[0];
                double value;
                Date date = null;

                if (nextLine[8].equals("")){
                    value = 0;
                } else {
                    value = Double.parseDouble(nextLine[8]);
                }

                try {
                    String stringDate = nextLine[1];
                    DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    date = df.parse(stringDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Price newPrice = new Price(companyName, value, date);
                allPrices.add(newPrice);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return allPrices;
    }

    public ArrayList<String> getColumnContents(int index) {
        String[] nextLine;
        ArrayList<String> columnContents = new ArrayList<String>();

        //Read the first line of the file (header file)
        int i = 0;
        try {
            while ((reader.readNext())!= null && i < 1){
                i += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Read the remaining lines of the file and store it into the ArrayList
        try {
            while ((nextLine = reader.readNext()) != null){
                columnContents.add(nextLine[index]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return columnContents;
    }


}
