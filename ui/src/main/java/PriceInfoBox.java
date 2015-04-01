import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by calvin on 27/03/15.
 */
public class PriceInfoBox extends InfoBox {

    public PriceInfoBox(double value, Date date, InfoBoxType type)
    {
        super(value, date, type);
    }

    protected Label makeInfoLabel(double value, Date date, InfoBoxType type)
    {
        final Label label = new Label(DateUtils.formatMonthAbbr(date) + "\n" +
                "Price: $" + value);
        label.getStyleClass().addAll("info-box");

        if (type.equals(InfoBoxType.Price)) {
            label.setTextFill(Color.BLACK);
        } else if (type.equals(InfoBoxType.BuyOrder)) {
            label.setTextFill(Color.FORESTGREEN);
        } else {
            label.setTextFill(Color.FIREBRICK);
        }

        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        return label;
    }
}
