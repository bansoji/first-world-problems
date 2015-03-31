import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by jasonlim on 30/03/15.
 */
public abstract class Parser<T> {
    protected CSVReader reader;
    protected static final Logger logger = Logger.getLogger("log");

    public Parser (String fileName) {
        try {
            this.reader = new CSVReader(new FileReader(fileName));
            try {
                reader.readNext();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public abstract T parseNextLine();
    public void close()
    {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
