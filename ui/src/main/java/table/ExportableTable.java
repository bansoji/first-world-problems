package table;

import image.ImageUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import report.ReportGenerator;

import java.io.File;

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
        final MenuItem exportToPDFItem = new MenuItem("Export to PDF", ImageUtils.getImage("icons/pdf.png"));
        ReportGenerator g = new ReportGenerator(this);
        exportToPDFItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Export PDF file");
                        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF file", ".pdf"));
                        File file = fileChooser.showSaveDialog(null);
                        if (file != null) {
                            new Thread() {
                                @Override
                                public void run() {
                                    g.generateTableReport(file.getAbsolutePath());
                                }
                            }.start();
                        }
                    }
                });
            }
        });
        final ContextMenu menu = new ContextMenu(exportToCSVItem, exportToPDFItem);
        setContextMenu(menu);
    }
}
