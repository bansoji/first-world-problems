import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by gavintam on 28/05/15.
 */
public class NowBuilder {
    public void buildCurrentStats(GridPane now) {
        Pane summary = buildSummary();
        GridPane.setConstraints(summary,0,0);
        now.getChildren().add(summary);
    }

    public Pane buildSummary() {
        GridPane summary = new GridPane();
        Label symbol = new Label("INDU");
        symbol.getStyleClass().add("sm-label");
        Stock stock = new Stock("INDU");
        Label price = new Label(stock.getStats().getBookValuePerShare().toPlainString());
        price.getStyleClass().add("lg-label");
        GridPane.setConstraints(symbol,0,0);
        GridPane.setConstraints(price,0,1);
        summary.getChildren().addAll(symbol,price);
        return summary;
    }
}
