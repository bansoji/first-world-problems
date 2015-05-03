package main;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by Edwin Li on 3/05/2015.
 */
public class Profit {
    private double returnValue;
    private DateTime pairDate;

    public Profit (double returnValue, DateTime pairDate) {
        this.returnValue = returnValue;
        this.pairDate = pairDate;
    }

    public double getProfitValue () {
        return this.returnValue;
    }

    public DateTime getProfitDate() {
        return this.pairDate;
    }
}
