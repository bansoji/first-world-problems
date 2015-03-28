package profit;

import java.util.Date;

/**
 * Created by Edwin on 27/03/2015.
 *
 * This class represents an individual asset that can be bought or sold.
 */
public class Asset {
    private String companyName; // The name of the company.
    private double buyPrice; // The price (per unit) that the asset was bought at.
    private int volume; // The number of units traded for this asset bundle.
    private Date date; // The date the asset was bought at

    /**
     * This constructor creates a new Asset.
     * @param companyName       The name of the company this asset is under.
     * @param buyPrice          The price per unit.
     * @param volume            The number of units traded.
     * @param date              The date this asset is traded at.
     */
    public Asset(String companyName, double buyPrice, int volume, Date date)
    {
        this.companyName = companyName;
        this.buyPrice = buyPrice;
        this.volume = volume;
        this.date = date;
    }

    /**
     * This method returns the company name under this Asset.
     * @return      The name of the company.
     */
    public String getCompanyName ()
    {
        return this.companyName;
    }

    /**
     * This method returns the price per unit this asset is bought at.
     * @return      The price per unit.
     */
    public double getBuyPrice ()
    {
        return this.buyPrice;
    }

    /**
     * This method returns the volume (total number of units) traded under this asset.
     * @return      The volume of units.
     */
    public int getVolume ()
    {
        return this.volume;
    }

    /**
     * This method returns the date that the asset was bought at.
     * @return      The date of trade.
     */
    public Date getDate ()
    {
        return this.date;
    }

}
