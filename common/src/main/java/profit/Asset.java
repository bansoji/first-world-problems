package profit;

import java.util.Date;

/**
 * Created by Edwin on 27/03/2015.
 */
public class Asset {
    private String companyName; // RIC (Name) of the Order.
    //private double price; // The price of the Order to place.
    private int volume; // The number of items to trade.
    //private Date date; // The date of the Order.]

    public Asset(String companyName, double price, int volume, Date date)
    {
        this.companyName = companyName;
        //this.price = price;
        this.volume = volume;
        //this.date = date;
    }

    public String getCompanyName ()
    {
        return this.companyName;
    }

    /*public double getPrice ()
    {
        return this.price;
    }*/

    public int getVolume ()
    {
        return this.volume;
    }

    /*public Date getDate ()
    {
        return this.date;
    }*/
}
