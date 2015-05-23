import date.DateUtils;
import graph.*;
import image.ImageUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Path;
import javafx.util.Callback;
import core.Order;
import core.OrderType;
import core.Price;
import org.joda.time.DateTime;
import table.ExportableTable;

import java.util.*;

/**
 * Created by Gavin Tam on 25/03/15.
 */
public class GraphBuilder {

    private CandleStickChart lineChart;
    private XYBarChart barChart;

    public void buildGraph(BorderPane graph, List<Price> prices, List<Order> orders, Map<DateTime, OrderType> orderSummary)
    {
        DateValueAxis xAxis = new DateValueAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        xAxis.setMinorTickVisible(false);
        yAxis.setLabel("Price");
        lineChart = new CandleStickChart(xAxis, yAxis);
        lineChart.getStyleClass().add("graph");
        yAxis.setForceZeroInRange(false);
        lineChart.setCacheHint(CacheHint.SPEED);

        DateValueAxis xAxisVolume = new DateValueAxis();
        NumberAxis yAxisVolume = new NumberAxis();
        xAxisVolume.setLabel("Date");
        xAxisVolume.setMinorTickVisible(false);
        yAxisVolume.setLabel("Order Volume");
        yAxisVolume.setForceZeroInRange(false);
        barChart = new XYBarChart(xAxisVolume, yAxisVolume);
        barChart.getStyleClass().add("graph");
        barChart.setCacheHint(CacheHint.SPEED);

        if (prices != null && prices.size() > 0) {
            //lineChart.setTitle("Price of " + prices.get(0).getCompanyName());
            XYChart.Series<Long, Number> priceChart = new XYChart.Series<>();
            XYChart.Series<Long, Number> volumeChart = new XYChart.Series<>();

            Iterator<Order> orderIterator = null;
            Order currOrder = null;
            if (orders != null) {
                orderIterator = orders.iterator();
                currOrder = orderIterator.next();
            }

            // populating the series with data
            for (int i = 0; i < prices.size(); i++) {
                ORDER_SEARCH:
                {
                    while (true) {
                        //if an order is placed at this price
                        if (currOrder != null && currOrder.getOrderDate().equals(prices.get(i).getDate())) {
                            NodeType type;
                            XYChart.Data volume;
                            if (currOrder.getOrderType().equals(OrderType.BUY)) {
                                type = NodeType.BuyOrder;
                                volume = new XYChart.Data<Long, Number>(currOrder.getOrderDate().getMillis(), currOrder.getVolume(),
                                        new XYBarChart.XYBarExtraValues(type));
                                changeBarColour(volume, "buy");
                            } else {
                                type = NodeType.SellOrder;
                                volume = new XYChart.Data<Long, Number>(currOrder.getOrderDate().getMillis(), currOrder.getVolume(),
                                        new XYBarChart.XYBarExtraValues(type));
                                changeBarColour(volume, "sell");
                            }
                            XYChart.Data price = new XYChart.Data<Long, Number>(prices.get(i).getDate().getMillis(), prices.get(i).getOpen(),
                                    new CandleStickChart.CandleStickExtraValues(type,
                                            prices.get(i).getValue(),
                                            prices.get(i).getHigh(),
                                            prices.get(i).getLow(),
                                            prices.get(i).getValue()));
                            priceChart.getData().add(price);
                            volumeChart.getData().add(volume);
                            //if no order is placed at this price
                        } else if (currOrder == null || currOrder.getOrderDate().isAfter(prices.get(i).getDate())) {
                            XYChart.Data price = new XYChart.Data<Long, Number>(prices.get(i).getDate().getMillis(), prices.get(i).getOpen(),
                                    new CandleStickChart.CandleStickExtraValues(NodeType.Price,
                                            prices.get(i).getValue(),
                                            prices.get(i).getHigh(),
                                            prices.get(i).getLow(),
                                            prices.get(i).getValue()));
                            priceChart.getData().add(price);
                            if (i == 0 || i == prices.size() - 1) {
                                XYChart.Data volume = new XYChart.Data<Long, Number>(prices.get(i).getDate().getMillis(), 0,
                                        new XYBarChart.XYBarExtraValues(NodeType.Price));
                                volumeChart.getData().add(volume);
                            }
                        } else if (orderIterator != null && orderIterator.hasNext()) {
                            currOrder = orderIterator.next();
                            continue;
                        }
                        break ORDER_SEARCH;
                    }
                }
            }
            barChart.getData().add(volumeChart);
            barChart.setLegendVisible(false);
            barChart.setPrefHeight(200);
            ObservableList<XYChart.Series<Long, Number>> data = lineChart.getData();
            if (data == null) {
                data = FXCollections.observableArrayList(priceChart);
                lineChart.setData(data);
            } else {
                lineChart.getData().add(priceChart);
            }
            // lineChart.getData().add(priceChart);
            lineChart.setLegendVisible(false);
            if (orders == null) {
                xAxisVolume.setLowerBound(xAxis.getLowerBound());
                yAxisVolume.setUpperBound(yAxis.getUpperBound());
            }
        }

        BorderPane pane = new BorderPane();
        pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                    xAxis.setAutoRanging(true);
                    yAxis.setAutoRanging(true);
                    xAxisVolume.setAutoRanging(true);
                    yAxisVolume.setAutoRanging(true);
                }
            }
        });
        if (prices != null && prices.size() > 0) syncGraphZooming();
        addMenu();
        pane.setCenter(ChartPanZoomManager.setup(lineChart));
        pane.setBottom(ChartPanZoomManager.setup(barChart));

        final VBox table = new VBox();
        table.setPadding(new javafx.geometry.Insets(0, 30, 30, 30));
        Pane tableView = buildTable(prices,orderSummary);
        table.getChildren().add(tableView);
        VBox.setVgrow(tableView,Priority.ALWAYS);

        graph.setCenter(pane);
        graph.setRight(table);
    }

    private void changeBarColour (XYChart.Data data, String type)
    {
        data.nodeProperty().addListener(new ChangeListener<Node>() {
            @Override
            public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
                if (newNode != null) {
                    newNode.getStyleClass().add("bar-" + type);
                }
            }
        });
    }

    private void syncGraphZooming()
    {
        syncZooming(lineChart,barChart);
        syncZooming(barChart,lineChart);
    }

    private void syncZooming(XYChart chart1, XYChart chart2) {
        ((ValueAxis)chart1.getXAxis()).lowerBoundProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                chart2.getXAxis().setAutoRanging(false);
                ((ValueAxis) chart2.getXAxis()).setLowerBound(((ValueAxis) chart1.getXAxis()).getLowerBound());
            }
        });
        ((ValueAxis)chart1.getXAxis()).upperBoundProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                chart2.getXAxis().setAutoRanging(false);
                ((ValueAxis) chart2.getXAxis()).setUpperBound(((ValueAxis) chart1.getXAxis()).getUpperBound());
            }
        });
    }

    public void updateUpperBound(long end) {
        lineChart.getXAxis().setAutoRanging(false);
        if (end != 0) {
            ((ValueAxis) lineChart.getXAxis()).setUpperBound(end);
        }
    }
    public void updateLowerBound(long start) {
        lineChart.getXAxis().setAutoRanging(false);
        if (start != 0) {
            ((ValueAxis) lineChart.getXAxis()).setLowerBound(start);
        }
    }


    private Pane buildTable(List<Price> prices, Map<DateTime,OrderType> orders) {
        TableView tableView = new TableView();

        TableColumn dateCol = new TableColumn("Date");
        dateCol.setMinWidth(100);
        dateCol.setComparator(new Comparator<String>(){
            @Override
            public int compare(String t1, String t2) {
                DateTime d1 = DateUtils.parseMonthAbbr(t1);
                DateTime d2 = DateUtils.parseMonthAbbr(t2);
                if (d1 == null || d2 == null) return -1;
                return Long.compare(d1.getMillis(),d2.getMillis());
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
                            if (orders.get(price.getDate()).equals(OrderType.BUY)) {
                                setStyle("-fx-control-inner-background: green");
                                //getStyleClass().add("buy-row");
                            } else if (orders.get(price.getDate()).equals(OrderType.SELL)) {
                                setStyle("-fx-control-inner-background: red");
                                //getStyleClass().add("sell-row");
                            }
                        } else {
                            setStyle("-fx-control-inner-background: white");
                            //getStyleClass().add("normal-row");
                        }
                    }
                };
                return row;
            }
        });

        final FilteredList<Price> filterableData;
        if (prices != null) {
            ObservableList<Price> data = FXCollections.observableArrayList(prices);
            filterableData = new FilteredList<>(data);
            tableView.setItems(filterableData);
        } else {
            filterableData = null;
        }
        tableView.getColumns().addAll(dateCol, priceCol);
        //ensures extra space to given to existing columns
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BorderPane pane = new BorderPane();
        pane.setCenter(tableView);
        pane.setTop(createToolBar(filterableData, orders));
        return pane;
    }

    private ToolBar createToolBar(FilteredList<Price> filterableData, Map<DateTime,OrderType> orders) {
        ToolBar toolbar = new ToolBar();
        toolbar.setId("prices-table-filters");
        final ToggleGroup group = new ToggleGroup();
        ToggleButton filterBuys = new ToggleButton("Buy", ImageUtils.getImage("icons/buy.png"));
        filterBuys.getStyleClass().add("table-filter");
        filterBuys.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (filterableData != null)
                    filterableData.setPredicate(price -> orders.containsKey(price.getDate()) && orders.get(price.getDate()).equals(OrderType.BUY));
            }
        });

        ToggleButton filterSells = new ToggleButton("Sell", ImageUtils.getImage("icons/sell.png"));
        filterSells.getStyleClass().add("table-filter");
        filterSells.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (filterableData != null)
                    filterableData.setPredicate(price -> orders.containsKey(price.getDate()) && orders.get(price.getDate()).equals(OrderType.SELL));
            }
        });

        ToggleButton showAll = new ToggleButton("All", ImageUtils.getImage("icons/all.png"));
        showAll.getStyleClass().add("table-filter");
        showAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (filterableData != null)
                    filterableData.setPredicate(price -> true);
            }
        });
        filterBuys.setToggleGroup(group);
        filterSells.setToggleGroup(group);
        showAll.setToggleGroup(group);
        showAll.setSelected(true);  //show all prices is the default selection
        toolbar.getItems().addAll(filterBuys, filterSells, showAll);
        return toolbar;
    }

    private void addMenu() {
        final MenuItem resetZoomItem = new MenuItem("Reset zoom", ImageUtils.getImage("icons/reset_zoom.png"));
        resetZoomItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                lineChart.getXAxis().setAutoRanging(true);
                lineChart.getYAxis().setAutoRanging(true);
            }
        });

        final MenuItem hideShowLineItem = new MenuItem("Hide Line", ImageUtils.getImage("icons/line_chart_hide.png"));
        hideShowLineItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                for (int seriesIndex=0; seriesIndex < lineChart.getData().size(); seriesIndex++) {
                    if (lineChart.getData().get(seriesIndex).getNode() instanceof Path) {
                        Path path = (Path)lineChart.getData().get(seriesIndex).getNode();
                        if (path.getOpacity() == 1) {
                            path.setOpacity(0);
                            hideShowLineItem.setText("Show Line");
                            hideShowLineItem.setGraphic(ImageUtils.getImage("icons/line_chart_show.png"));
                        } else if (path.getOpacity() == 0) {
                            path.setOpacity(1);
                            hideShowLineItem.setText("Hide Line");
                            hideShowLineItem.setGraphic(ImageUtils.getImage("icons/line_chart_hide.png"));
                        }
                    }
                }
                lineChart.layout();
            }
        });

        final ContextMenu menu = new ContextMenu(
                resetZoomItem, new SeparatorMenuItem(), hideShowLineItem
        );

        lineChart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                if (event.getButton().equals(MouseButton.MIDDLE)
                        || (event.isShiftDown() && event.getButton().equals(MouseButton.PRIMARY))) {
                    if (menu.isShowing()) {
                        menu.hide();
                    } else {
                        menu.show(lineChart, event.getScreenX(), event.getScreenY());
                    }
                }
            }
        });
    }
}
