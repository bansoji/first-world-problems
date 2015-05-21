package table;

import image.ImageUtils;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

/**
 * Created by gavintam on 21/05/15.
 */
public class ExportChartableTable extends ExportableTable {

    MenuItem chartItem;

    public ExportChartableTable() {
        super();
        setupMenu();
    }

    public ExportChartableTable(ObservableList items) {
        super(items);
        setupMenu();
    }

    public void setupMenu() {
        super.setupMenu();
        chartItem = new MenuItem("Chart", ImageUtils.getImage("icons/graphs_pie.png"));
        addToMenu(chartItem, 0);
    }

    public void setChartAction(EventHandler<ActionEvent> action) {
        chartItem.setOnAction(action);
    }
}
