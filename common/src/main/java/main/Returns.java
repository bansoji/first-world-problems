package main;

/**
 * Created by Zippy on 19/05/2015.
 * This class is an encapsulation of a group of different returns.
 */
public class Returns {


    private double returns;
    private double percent;
    private double bought;


    /**
     * Initialises the private variables. Said variables can also be changed with "setX"
     */
    public Returns () {
        this.returns = 0.00;    //The return value (in dollars).
        this.percent = 0.00;    //The return percent.
        this.bought = 0.00;     //The buy value (in dollars).
    }

    public void addReturns(double returns) {
        this.returns += returns;
    }

    public void addPercent(double percent) {
        this.percent += percent;
    }

    public void addBought(double bought) {
        this.bought += bought;
    }

    public void setReturns(double returns) {
        this.returns = returns;
    }

    public void setBought(double bought) {
        this.bought = bought;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public double getReturns() {
        return returns;
    }

    public double getPercent() {
        return percent;
    }

    public double getBought() {
        return bought;
    }
}
