import date.DateUtils;
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
                Date d1 = DateUtils.parseMonthAbbr(t1);
                Date d2 = DateUtils.parseMonthAbbr(t2);
                if (d1 == null || d2 == null) return -1;
                return Long.compare(d1.getTime(),d2.getTime());
            }
        });
        dateCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Price, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Price, String> p) {
                if (p.getValue() != null) {
                    return new SimpleStringProperty(DateUtils.formatMonthAbbr(p.getValue().getDate()));
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
        //ensures extra space to given to existing columns
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);



        final VBox vbox = new VBox();
        vbox.setStyle("-fx-background-color: white");
        vbox.setSpacing(5);
        vbox.setPadding(new javafx.geometry.Insets(0, 20, 0, 10));
        vbox.getChildren().addAll(tableView);

        Scene scene = new Scene(vbox);
        table.setScene(scene);
    }
}
