package table;

import image.ImageUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * Created by gavintam on 21/05/15.
 */
public class ExportableTable extends OptionsTable {
    public ExportableTable() {
        super();
        setupMenu();
    }

    public ExportableTable(ObservableList items) {
        super(items);
        setupMenu();
    }

    public void setupMenu() {
        final MenuItem exportToCSVItem = new MenuItem("Export to CSV", ImageUtils.getImage("icons/csv.png"));
        exportToCSVItem.setOnAction(TableExporter.exportToCSV(this));
        final ContextMenu menu = new ContextMenu(exportToCSVItem);
        setContextMenu(menu);
    }
}
