import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.util.Callback;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Gavin Tam on 27/03/15.
 */
public class TableBuilder {
    public static void buildTable(JFXPanel table, List<Price> prices, Map<Date,OrderType> orders)
    {
        TableView tableView = new TableView();

        TableColumn dateCol = new TableColumn("Date");
        dateCol.setMinWidth(100);
        dateCol.setComparator(new Comparator<String>(){
            @Override
            public int compare(String t1, String t2) {
                try{
                    SimpleDateFormat format =new SimpleDateFormat("dd MMM yyyy");
                    Date d1 = format.parse(t1);
                    Date d2 = format.parse(t2);
                    return Long.compare(d1.getTime(),d2.getTime());
                } catch (ParseException p){
                    p.printStackTrace();
                }
                return -1;
            }
        });
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        dateCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Price, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Price, String> p) {
                if (p.getValue() != null) {
                    return new SimpleStringProperty(df.format(p.getValue().getDate()));
                } else {
                    return new SimpleStringProperty("-");
                }
            }
        });

        TableColumn priceCol = new TableColumn("Price");
        priceCol.setMinWidth(100);
        priceCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Price, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Price, String> p) {
                if (p.getValue() != null) {
                    return new SimpleStringProperty(String.valueOf(p.getValue().getValue()));
                } else {
                    return new SimpleStringProperty("-");
                }
            }
        });

        tableView.setRowFactory(new Callback<TableView<Price>, TableRow<Price>>() {
            @Override
            public TableRow<Price> call(TableView<Price> tableView) {
                final TableRow<Price> row = new TableRow<Price>() {
                    @Override
                    protected void updateItem(Price price, boolean empty) {
                        super.updateItem(price, empty);
                        if (price != null && !empty && orders.get(price.getDate()) != null) {
                            setTextFill(Color.WHITE);
                            if (orders.get(price.getDate()).equals(OrderType.BUY)) {
                                setStyle("-fx-control-inner-background: green");
                            } else if (orders.get(price.getDate()).equals(OrderType.SELL)) {
                                setStyle("-fx-control-inner-background: red");
                            }
                        } else {
                            setStyle("-fx-control-inner-background: white");
                        }
                    }
                };
                return row;
            }
        });

        ObservableList<Price> data = FXCollections.observableArrayList(prices);
        tableView.setItems(data);
        tableView.getColumns().addAll(dateCol, priceCol);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new javafx.geometry.Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(tableView);

        Scene scene = new Scene(vbox);
        table.setScene(scene);
    }
}
