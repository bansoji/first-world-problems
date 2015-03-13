package main.java;

import java.util.Date;

/**
 * This class represents the price, parsed from the input file.
 */
public class Price {
    private String companyName; // The company name, identical to RIC from the input file. 
    private double value; // The value of the share.
    private Date date;

    public Price(String companyName, double value, Date date){
        this.companyName = companyName;
        this.value = value;
        this.date = date;
    }

    public String getCompanyName(){
        return this.companyName;
    }

    public double getValue(){
        return this.value;
    }

    public Date getDate() {
        return date;
    }
}
