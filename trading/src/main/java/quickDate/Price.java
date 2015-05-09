package quickDate;

import org.joda.time.DateTime;

import java.util.logging.Logger;

/**
 * This class represents the price, parsed from the input file.
 */
public class Price {
    private String companyName; // The company name, identical to RIC from the input file.
    private double value; // The value of the share.
    private String date;
    private double high;
    private double low;
    private double open;

    private int volume;

    private static final Logger logger = Logger.getLogger("log");

    public Price(String companyName, double value, String date, double open, double high, double low, int vol){
        this.companyName = companyName;
        this.value = value;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.volume= vol;
    }

    public String getCompanyName(){
        return this.companyName;
    }

    public double getValue(){
        return this.value;
    }

    public String getDate() {
        return date;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public int getVolume() {
        return volume;
    }

}
