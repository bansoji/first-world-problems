package table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.Map;

/**
 * Created by gavintam on 23/05/15.
 */
public class TableUtils {
    public enum ColumnType {
        Integer,
        Double,
        String
    }
    public static TableColumn createMapColumn(String name, ColumnType type) {
        TableColumn col = new TableColumn(name);
        col.setCellValueFactory(new MapValueFactory<>(name));
        if (type.equals(ColumnType.Integer)) {
            col.setCellFactory(new Callback<TableColumn<Map, Object>,
                    TableCell<Map, Object>>() {
                @Override
                public TableCell call(TableColumn p) {
                    return new TextFieldTableCell(new IntegerStringConverter());
                }
            });
        } else if (type.equals(ColumnType.Double)) {
            col.setCellFactory(new Callback<TableColumn<Map, Object>,
                    TableCell<Map, Object>>() {
                @Override
                public TableCell call(TableColumn p) {
                    return new TextFieldTableCell(new DoubleStringConverter());
                }
            });
        } else {
            col.setCellFactory(new Callback<TableColumn<Map, Object>,
                    TableCell<Map, Object>>() {
                @Override
                public TableCell call(TableColumn p) {
                    return new TextFieldTableCell(new StringConverter() {
                        @Override
                        public String toString(Object t) {
                            return t.toString();
                        }

                        @Override
                        public Object fromString(String string) {
                            return string;
                        }
                    });
                }
            });
        }
        return col;
    }
}
