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
public abstract class InfoBox extends StackPane {
    public enum InfoBoxType
    {
        BuyOrder,
        SellOrder,
        Price;
    }

    public InfoBox(double value, Date date, InfoBoxType type) {
        setPrefSize(10, 10);
        final Label label = createInfoLabel(value, date, type);

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

    private Label createInfoLabel(double value, Date date, InfoBoxType type) {
        if (date == null) {
            return new Label("No data available");
        } else
        {
            return makeInfoLabel(value,date,type);
        }
    }

    protected abstract Label makeInfoLabel(double value, Date date, InfoBoxType type);
}
