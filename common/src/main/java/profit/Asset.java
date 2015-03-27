package profit;

import java.util.Date;

/**
 * Created by Edwin on 27/03/2015.
 */
public class Asset {
    private String companyName; // The name of the company.
    private double buyPrice; // The price (per unit) that the asset was bought at.
    private int volume; // The number of units bought for this asset bundle.
    //private Date date; // The date the asset was bought at

    public Asset(String companyName, double buyPrice, int volume/*, Date date*/)
    {
        this.companyName = companyName;
        this.buyPrice = buyPrice;
        this.volume = volume;
        //this.date = date;
    }

    public String getCompanyName ()
    {
        return this.companyName;
    }

    public double getPrice ()
    {
        return this.buyPrice;
    }

    public int getVolume ()
    {
        return this.volume;
    }

    /*public Date getDate ()
    {
        return this.date;
    }*/
}
