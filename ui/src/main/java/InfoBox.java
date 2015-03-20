import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Sourced from https://gist.github.com/jewelsea/4681797
 */
public class InfoBox extends StackPane {
    public enum InfoBoxType
    {
        BuyOrder,
        SellOrder,
        Price;
    }
    public InfoBox(double value, Date date, InfoBoxType type) {
        setPrefSize(10, 10);
        if (type.equals(InfoBoxType.BuyOrder)) {
            getStyleClass().add("chart-line-symbol-buy");
        } else if (type.equals(InfoBoxType.SellOrder)) {
            getStyleClass().add("chart-line-symbol-sell");
        } else {
            getStyleClass().add("chart-line-symbol-price");
        }
        final Label label = createDataThresholdLabel(value, date, type);

        setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
            }
        });
        setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
            }
        });
    }

    private Label createDataThresholdLabel(double value, Date date, InfoBoxType type) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        final Label label = new Label(dateFormat.format(date) + "\n" +
                                    "Price: $" + value);
        label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
        label.setStyle("-fx-font-size: 12;");

        if (type.equals(InfoBoxType.Price)) {
            label.setTextFill(Color.DARKGRAY);
        } else if (type.equals(InfoBoxType.BuyOrder)) {
            label.setTextFill(Color.FORESTGREEN);
        } else {
            label.setTextFill(Color.FIREBRICK);
        }

        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        return label;
    }
}
