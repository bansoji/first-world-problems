import date.DateUtils;
import graph.CandleStickChart;
import graph.DateValueAxis;
import graph.NodeType;
import graph.XYBarChart;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.*;

/**
 * Created by Gavin Tam on 27/03/15.
 */
public class StatsBuilder {

    public static void build(JFXPanel stats, History<Order> history, List<Price> prices, Map<Date, OrderType> orders) {
        final VBox vbox = new VBox();
        vbox.setStyle("-fx-background-color: white");
        vbox.setSpacing(5);
        vbox.setPadding(new javafx.geometry.Insets(0, 20, 0, 10));
        vbox.getChildren().addAll(buildPortfolioStats(history),buildTable(prices, orders));
        Scene scene = new Scene(vbox);
        stats.setScene(scene);
    }

    private static TableView buildTable(List<Price> prices, Map<Date,OrderType> orders) {
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
        return tableView;
    }

    private static BarChart<Number,String> buildPortfolioStats(History<Order> history) {

        CategoryAxis yAxis = new CategoryAxis();
        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setOpacity(0);
        yAxis.setTickMarkVisible(false);
        BarChart<Number,String> barChart = new BarChart(xAxis, yAxis);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setHorizontalGridLinesVisible(false);
        barChart.setHorizontalZeroLineVisible(false);

        Portfolio portfolio = new Portfolio(history);
        Map<String,Double> portfolioValue = portfolio.getPortfolioValue();
        XYChart.Series<Number,String> portfolioStats = new XYChart.Series<>();

        if (portfolioValue.size() > 0) {
            double totalPortfolioValue = 0;
            for (String company : portfolioValue.keySet()) {
                totalPortfolioValue += portfolioValue.get(company);
            }
            XYChart.Data value = new XYChart.Data<Number,String>(totalPortfolioValue, "Portfolio");
            value.nodeProperty().addListener(new ChangeListener<Node>() {
                @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                    if (node != null) {
                        displayLabelForData(value);
                    }
                }
            });
            portfolioStats.getData().add(value);
        }

        Map<String,Double> equityValue = portfolio.getAssetValue();
        if (equityValue.size() > 0) {
            double totalEquityValue = 0;
            for (String company : equityValue.keySet()) {
                totalEquityValue += equityValue.get(company);
            }
            XYChart.Data value = new XYChart.Data<Number,String>(totalEquityValue, "Equity");
            value.nodeProperty().addListener(new ChangeListener<Node>() {
                @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                    if (node != null) {
                        displayLabelForData(value);
                    }
                }
            });
            portfolioStats.getData().add(value);
        }
        barChart.getData().add(portfolioStats);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(150);
        barChart.setPrefWidth(300);
        return barChart;
    }

    /** places a text label with a bar's value above a bar node for a given XYChart.Data */
    private static void displayLabelForData(XYChart.Data<Number, String> data) {
        final Node node = data.getNode();
        final Text dataText = new Text("$" + data.getXValue());
        node.parentProperty().addListener(new ChangeListener<Parent>() {
            @Override public void changed(ObservableValue<? extends Parent> ov, Parent oldParent, Parent parent) {
                Group parentGroup = (Group) parent;
                parentGroup.getChildren().add(dataText);
            }
        });

        node.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
            @Override public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
                dataText.setLayoutX(
                        Math.round(
                                bounds.getMaxX() + 25
                        )
                );
                dataText.setLayoutY(
                        Math.round(
                                bounds.getMaxY() - dataText.prefHeight(-1) * 0.5
                        )
                );
            }
        });
    }
}
