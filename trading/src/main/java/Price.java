package main.java;

/**
 * This class represents the price, parsed from the input file.
 */
public class Price {
    private String companyName; // The company name, identical to RIC from the input file. 
    private double value; // The value of the share. 

    public Price(String companyName, double value){
        this.companyName = companyName;
        this.value = value;
    }

    public String getCompanyName(){
        return this.companyName;
    }

    public double getValue(){
        return this.value;
    }
}
