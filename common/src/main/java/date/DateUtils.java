package date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.logging.Logger;

/**
 * Created by Gavin Tam on 31/03/15.
 */
public class DateUtils {

    private static final DateTimeFormatter df = DateTimeFormat.forPattern("dd-MM-yyyy");
    private static final DateTimeFormatter monthAbbrFormat = DateTimeFormat.forPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter yearFirstFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final Logger logger = Logger.getLogger("log");
    private static final String dd_MM_yyyy = "\\d{1,2}-\\d{1,2}-\\d{4}";
    private static final String dd_MMM_yyyy = "\\d{1,2}-[a-zA-Z]{1,3}-\\d{4}";
    private static final String yyyy_MM_dd = "\\d{4}-\\d{1,2}-\\d{1,2}";

    public static String format(DateTime date)
    {
        return df.print(date);
    }

    public static String formatMonthAbbr(DateTime date)
    {
        return monthAbbrFormat.print(date);
    }

    public static String formatYearFirst(DateTime date) {
        return yearFirstFormat.print(date);
    }

    public static DateTime parse(String string, String parseErrorMessage)
    {
        try {
            return df.parseDateTime(string).withZone(DateTimeZone.getDefault());
        } catch (Exception e) {
            logger.warning(parseErrorMessage + " " + string);
            return null;
        }
    }

    public static DateTime parse(String string)
    {
        return parse(string, "Error parsing date.");
    }

    public static DateTime parseMonthAbbr(String string, String parseErrorMessage)
    {
        try {
            return monthAbbrFormat.parseDateTime(string).withZone(DateTimeZone.getDefault());
        } catch (Exception e) {
            logger.warning(parseErrorMessage);
            return null;
        }
    }

    public static DateTime parseMonthAbbr(String string)
    {
        return parseMonthAbbr(string, "Error parsing date.");
    }

    public static DateTime parseYearFirst(String string, String parseErrorMessage)
    {
        try {
            return yearFirstFormat.parseDateTime(string).withZone(DateTimeZone.getDefault());
        } catch (Exception e) {
            logger.warning(parseErrorMessage);
            return null;
        }
    }

    public static DateTime parseYearFirst(String string)
    {
        return parseYearFirst(string, "Error parsing date.");
    }


    //compare two date strings
    public static Boolean before(String s1, String s2) {
        //if one of the strings is not in the required date formats
        if (!(s1.matches(dd_MMM_yyyy) || s1.matches(dd_MM_yyyy) || s1.matches(yyyy_MM_dd))
                || !(s2.matches(dd_MMM_yyyy) || s2.matches(dd_MM_yyyy) || s2.matches(yyyy_MM_dd)))
            return null;

        String[] tokens1 = s1.split("-");
        String[] tokens2 = s2.split("-");

        //if month is MMM
        if (tokens1[1].length() == 3) tokens1[1] = convertToMonthNum(tokens1[1]);
        if (tokens2[1].length() == 3) tokens2[1] = convertToMonthNum(tokens2[1]);

        //format date string to YYYYMMDD
        String t1;
        if (s1.matches(yyyy_MM_dd)) {
            t1 = tokens1[0] + (tokens1[1].length() == 1 ? "0" + tokens1[1] : tokens1[1]) + (tokens1[2].length() == 1 ? "0" + tokens1[2] : tokens1[2]);
        } else {
            t1 = tokens1[2] + (tokens1[1].length() == 1 ? "0" + tokens1[1] : tokens1[1]) + (tokens1[0].length() == 1 ? "0" + tokens1[0] : tokens1[0]);
        }

        String t2;
        if (s2.matches(yyyy_MM_dd)) {
            t2 = tokens2[0] + (tokens2[1].length() == 1 ? "0" + tokens2[1] : tokens2[1]) + (tokens2[2].length() == 1 ? "0" + tokens2[2] : tokens2[2]);
        } else {
            t2 = tokens2[2] + (tokens2[1].length() == 1 ? "0" + tokens2[1] : tokens2[1]) + (tokens2[0].length() == 1 ? "0" + tokens2[0] : tokens2[0]);
        }
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
