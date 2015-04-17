import date.DateUtils;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.util.Date;

/**
 * Created by Gavin Tam on 27/03/15.
 */

@Deprecated
public class VolumeInfoBox extends InfoBox {

    public VolumeInfoBox(double value, Date date, InfoBoxType type)
    {
        super(value, date, type);
    }

    protected Label makeInfoLabel(double value, Date date, InfoBoxType type)
    {
        //price is not valid
        if (type.equals(InfoBoxType.Price)) return new Label();
        final Label label = new Label(DateUtils.formatMonthAbbr(date) + "\n" +
                "Volume: " + value);
        label.getStyleClass().addAll("info-box");

        if (type.equals(InfoBoxType.BuyOrder)) {
            label.setTextFill(Color.FORESTGREEN);
        } else {
            label.setTextFill(Color.FIREBRICK);
        }

        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        return label;
    }
}
