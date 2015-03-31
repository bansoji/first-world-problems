import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Gavin Tam on 31/03/15.
 */
public class DateUtils {

    private static final DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    private static final DateFormat monthAbbrFormat = new SimpleDateFormat("dd-MMM-yyyy");
    private static final Logger logger = Logger.getLogger("log");

    public static String format(Date date)
    {
        return df.format(date);
    }

    public static String formatMonthAbbr(Date date)
    {
        return monthAbbrFormat.format(date);
    }

    public static Date parse(String string, String parseErrorMessage)
    {
        try {
            return df.parse(string);
        } catch (ParseException e) {
            logger.warning(parseErrorMessage);
            return null;
        }
    }

    public static Date parse(String string)
    {
        return parse(string, "Error parsing date.");
    }

    public static Date parseMonthAbbr(String string, String parseErrorMessage)
    {
        try {
            return monthAbbrFormat.parse(string);
        } catch (ParseException e) {
            logger.warning(parseErrorMessage);
            return null;
        }
    }

    public static Date parseMonthAbbr(String string)
    {
        return parseMonthAbbr(string, "Error parsing date.");
    }
}
