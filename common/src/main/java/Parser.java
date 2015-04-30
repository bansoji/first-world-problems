import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by jasonlim on 30/03/15.
 */
public abstract class Parser<T> {
    protected CSVReader reader;
    protected static final Logger logger = Logger.getLogger("log");

    // /Records the number of file lines
    protected int numberOfFileLines;

    public Parser (String fileName) {
        try {
            this.reader = new CSVReader(new BufferedReader(new FileReader(fileName)));
            try {
                //skip first line
                reader.readNext();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        numberOfFileLines = 0;
    }

    public abstract List<T> parseAllLines();

    public void close()
    {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNumberOfFileLines() {
        return numberOfFileLines;
    }
}
