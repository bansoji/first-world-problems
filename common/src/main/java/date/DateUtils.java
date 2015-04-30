package date;

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
        } catch (NullPointerException | ParseException e) {
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
        } catch (NullPointerException | ParseException e) {
            logger.warning(parseErrorMessage);
            return null;
        }
    }

    public static Date parseMonthAbbr(String string)
    {
        return parseMonthAbbr(string, "Error parsing date.");
    }

    public static Boolean before(String s1, String s2) {
        boolean s1ddMMMyyyy = s1.matches("\\d{1,2}-[a-zA-Z]{1,3}-\\d{4}");
        boolean s2ddMMMyyyy = s2.matches("\\d{1,2}-[a-zA-Z]{1,3}-\\d{4}");
        if (!(s1ddMMMyyyy && s1.matches("\\d{1,2}-\\d{1,2}-\\d{4}"))
                && !(s2ddMMMyyyy && s2.matches("\\d{1,2}-\\d{1,2}-\\d{4}")))
            return null;

        String[] tokens1 = s1.split("-");
        String[] tokens2 = s2.split("-");

        if (tokens1[1].length() == 3) tokens1[1] = convertToMonthNum(tokens1[1]);
        if (tokens2[1].length() == 3) tokens2[1] = convertToMonthNum(tokens2[1]);

        String t1 = tokens1[2] + (tokens1[1].length() == 1 ? "0" + tokens1[1] : tokens1[1]) + (tokens1[0].length() == 1 ? "0" + tokens1[0] : tokens1[0]);
        String t2 = tokens2[2] + (tokens2[1].length() == 1 ? "0" + tokens2[1] : tokens2[1]) + (tokens2[0].length() == 1 ? "0" + tokens2[0] : tokens2[0]);
        return (Integer.parseInt(t1) < Integer.parseInt(t2));
    }

    public static boolean after (String s1, String s2) {
        return before(s2,s1);
    }

    private static String convertToMonthNum(String month) {
        month = month.toUpperCase();
        switch(month) {
            case "JAN": return "01";
            case "FEB": return "02";
            case "MAR": return "03";
            case "APR": return "04";
            case "MAY": return "05";
            case "JUN": return "06";
            case "JUL": return "07";
            case "AUG": return "08";
            case "SEP": return "09";
            case "OCT": return "10";
            case "NOV": return "11";
            case "DEC": return "12";
            default: return null;
        }
    }
}
