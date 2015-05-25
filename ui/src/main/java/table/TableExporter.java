package table;

import com.opencsv.CSVWriter;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import report.ReportGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by gavintam on 21/05/15.
 */
public class TableExporter {
    private static final Logger logger = Logger.getLogger("application_log");

    public static EventHandler<ActionEvent> exportToCSV(OptionsTable table) {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Export CSV file");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file", "*.csv"));
                File file = fileChooser.showSaveDialog(null);
                if (file != null) {
                    try {
                        exportCSV(table, file.getAbsolutePath());
                    } catch (IOException e) {
                        logger.severe("CSV export for " + file.getAbsolutePath() + " was unsuccessful.");
                    }
                }
            }
        };
    }

    private static void exportCSV(TableView tv, String filename) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(filename), ',', CSVWriter.NO_QUOTE_CHARACTER);

        // Produce headers.
        ObservableList<TableColumn> cols = tv.getColumns();
        List<String> columnHeaders = new ArrayList<>();
        for (TableColumn col : cols) {
            columnHeaders.add(col.getText());
        }

        String[] row = new String[columnHeaders.size()];
        row = columnHeaders.toArray(row);

        writer.writeNext(row);

        // Produce Data.
        ObservableList<Map> rows = tv.getItems();
        for (Map rowData : rows){
            List<String> rowItems = new ArrayList();
            for (String column: columnHeaders) {
                rowItems.add(rowData.get(column).toString());
            }
            String[] rowWrite = new String[rowItems.size()];
            rowWrite = rowItems.toArray(rowWrite);
            writer.writeNext(rowWrite);
        }

        writer.close();
    }
}
