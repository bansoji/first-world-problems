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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

import javax.sound.sampled.Port;
import java.util.*;

/**
 * Created by Gavin Tam on 27/03/15.
 */
public class StatsBuilder {

    public static void build(Pane stats, History<Order> history, List<Price> prices, Map<Date, OrderType> orders) {
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        Portfolio portfolio = new Portfolio(history);
        vbox.getChildren().addAll(buildPortfolioStats(portfolio));

        final HBox hbox = new HBox();
        hbox.setSpacing(20);
        hbox.getChildren().addAll(vbox,buildTable(portfolio.getReturns()));
        hbox.setPadding(new javafx.geometry.Insets(15, 20, 20, 15));
        stats.getChildren().setAll(hbox);
    }

    private static BarChart<Number,String> buildPortfolioStats(Portfolio portfolio) {

        CategoryAxis yAxis = new CategoryAxis();
        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setOpacity(0);
        yAxis.setTickMarkVisible(false);
        BarChart<Number,String> barChart = new BarChart(xAxis, yAxis);
        barChart.setId("portfolio");

        Map<String,Double> portfolioValue = portfolio.getPortfolioValue();
        XYChart.Series<Number,String> portfolioStats = new XYChart.Series<>();

        double totalPortfolioValue = 0;
        for (String company : portfolioValue.keySet()) {
            totalPortfolioValue += portfolioValue.get(company);
        }
        XYChart.Data valuePortfolio = new XYChart.Data<Number,String>(totalPortfolioValue, "Portfolio");
        valuePortfolio.nodeProperty().addListener(new ChangeListener<Node>() {
            @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                if (node != null) {
                    displayLabelForData(valuePortfolio);
                }
            }
        });
        portfolioStats.getData().add(valuePortfolio);

        Map<String,Double> equityValue = portfolio.getAssetValue();
        double totalEquityValue = 0;
        for (String company : equityValue.keySet()) {
            totalEquityValue += equityValue.get(company);
        }
        XYChart.Data valueEquity = new XYChart.Data<Number,String>(totalEquityValue, "Equity");
        valueEquity.nodeProperty().addListener(new ChangeListener<Node>() {
            @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                if (node != null) {
                    displayLabelForData(valueEquity);
                }
            }
        });
        portfolioStats.getData().add(valueEquity);

        barChart.getData().add(portfolioStats);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(150);
        barChart.setPrefWidth(500);
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
                        Math.round(bounds.getMaxX() + 25)
                );
                dataText.setLayoutY(
                        Math.round(bounds.getMaxY() - dataText.prefHeight(-1) * 0.5)
                );
            }
        });
    }

    private static TableView buildTable(Map<String,List<Double>> assetValue) {

        ObservableList<Map> data = FXCollections.observableArrayList();
        for (String company: assetValue.keySet()) {
            Map<String,Object> row = new HashMap<String,Object>();
            row.put("Company",company);
            row.put("Return", (double)Math.round(assetValue.get(company).get(0)*100)/100);
            row.put("Return %", (double)Math.round(assetValue.get(company).get(1)*100)/100);
            data.add(row);
        }

        TableView tableView = new TableView(data);

        TableColumn companyCol = new TableColumn("Company");
        companyCol.setMinWidth(100);
        companyCol.setCellValueFactory(new MapValueFactory<>("Company"));

        companyCol.setCellFactory(new Callback<TableColumn<Map, Object>,
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

        Callback<TableColumn<Map, Object>, TableCell<Map, Object>>
                returnCallback = new Callback<TableColumn<Map, Object>,
                TableCell<Map, Object>>() {
            @Override
            public TableCell call(TableColumn p) {
                return new TextFieldTableCell(new DoubleStringConverter());
            }
        };

        TableColumn returnCol = new TableColumn("Return");
        returnCol.setMinWidth(100);
        returnCol.setCellValueFactory(new MapValueFactory<>("Return"));
        returnCol.setCellFactory(returnCallback);

        TableColumn returnPercentCol = new TableColumn("Return %");
        returnPercentCol.setMinWidth(100);
        returnPercentCol.setCellValueFactory(new MapValueFactory<>("Return %"));
        returnPercentCol.setCellFactory(returnCallback);


        tableView.getColumns().addAll(companyCol, returnCol, returnPercentCol);
        //ensures extra space to given to existing columns
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return tableView;
    }
}
