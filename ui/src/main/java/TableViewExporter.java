import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import org.apache.poi.ss.usermodel.CellValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class exports a table view.
 */
public class TableViewExporter {

    public static void exportCsv(TableView tv, String filename) throws IOException {

        /*
        CSVWriter writer = new CSVWriter(new FileWriter(filename), ',', CSVWriter.NO_QUOTE_CHARACTER);
        // feed in your array (or convert your data to an array)
        String[] entries = "first#second#third".split("#");
        writer.writeNext(entries);
        writer.close();

        */

        ObservableList<TableColumn> cols = tv.getColumns();
        List<String> columns = new ArrayList<>();
        for (TableColumn col : cols){
            //columns.add(col.getText());

            System.out.println();
        }


    }
}
