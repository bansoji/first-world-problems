import com.opencsv.CSVWriter;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import org.apache.poi.ss.usermodel.CellValue;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class exports a table view.
 */
public class TableViewExporter {

    public static void exportCsv(TableView tv, String filename) throws IOException {


        CSVWriter writer = new CSVWriter(new FileWriter(filename), ',', CSVWriter.NO_QUOTE_CHARACTER);

        // Produce headers.
        ObservableList<TableColumn> cols = tv.getColumns();
        List<String> columnHeaders = new ArrayList<>();
        for (TableColumn col : cols){
            columnHeaders.add(col.getText());

            System.out.println(col.getText());
        }

        String[] row = new String[columnHeaders.size()];
        row = columnHeaders.toArray(row);

        writer.writeNext(row);

        // Produce Data.
        // TODO

        writer.close();

    }
}
