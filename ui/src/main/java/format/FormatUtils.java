package format;

/**
 * Created by Gavin Tam on 2/05/15.
 */
public class FormatUtils {

    //adds dollar sign to a price and places the negative sign on the outside
    public static String formatPrice(double price) {
        if (price < 0) {
            return "-$" + (-round2dp(price));
        } else {
            return "$" + round2dp(price);
        }
    }

    public static double round2dp (double n) {
        return (double)Math.round(n*100)/100;
    }

    public static double round3dp (double n) {
        return (double)Math.round(n*1000)/1000;
    }

    public static double round5dp (double n) {
        return (double)Math.round(n*100000)/100000;
    }
}
