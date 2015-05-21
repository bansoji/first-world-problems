package table;

import image.ImageUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

/**
 * Created by gavintam on 21/05/15.
 */
public abstract class OptionsTable extends TableView {
    public OptionsTable() {
        super();
        setupMenu();
    }

    public OptionsTable(ObservableList items) {
        super(items);
        setupMenu();
    }

    public abstract void setupMenu();

    public void addToMenu(MenuItem item) {
        getContextMenu().getItems().add(item);
    }

    public void addToMenu(MenuItem item, int index) {
        getContextMenu().getItems().add(index,item);
    }

    public void addAllToMenu(MenuItem ... items) {
        getContextMenu().getItems().addAll(items);
    }
}
