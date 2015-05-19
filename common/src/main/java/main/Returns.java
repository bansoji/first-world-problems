package main;

/**
 * Created by Zippy on 19/05/2015.
 */
public class Returns {


    private double returns;
    private double percent;
    private double bought;

    public Returns () {
        this.returns = 0.00;
        this.percent = 0.00;
        this.bought = 0.00;
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
