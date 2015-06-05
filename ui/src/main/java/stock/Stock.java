package stock;

/**
 * Created by gavintam on 29/05/15.
 */
public class Stock {
    private String name;
    private String symbol;
    private String price;
    private String percent;

    public Stock(String name, String symbol, String price, String percent) {
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.percent = percent;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
