import components.AutoCompleteComboBoxListener;
import components.DateRangeFilterBuilder;
import components.LabelledSelector;
import components.TitleBox;
import core.*;
import date.DateUtils;
import dialog.DialogBuilder;
import graph.*;
import image.ImageUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Path;
import javafx.util.Callback;
import org.joda.time.DateTime;

import java.util.*;

/**
 * Created by Gavin Tam on 25/03/15.
 */
public class GraphBuilder {

    private BorderPane graph;
    private ComboBox<String> companySelector;
    private Button profile;
    private ChangeListener companyListener;

    private Reader priceReader;
    private Reader orderReader;

    private CandleStickChart lineChart;
    private XYBarChart barChart;
    private BorderPane table;

    public void buildGraph(BorderPane graph, List<Price> prices, List<Order> orders,
                           Reader priceReader, Reader orderReader, Map<DateTime, OrderType> orderSummary)
    {
        if (lineChart == null || barChart == null) {
            init(graph);
        }
        this.graph = graph;
        if (prices != null && prices.size() > 0) {
            if (lineChart.getData() != null) lineChart.getData().clear();
            if (barChart.getData() != null) barChart.getData().clear();

            XYChart.Series<Long, Number> priceChart = new XYChart.Series<>();
            XYChart.Series<Long, Number> volumeChart = new XYChart.Series<>();

            Iterator<Order> orderIterator = null;
            Order currOrder = null;
            if (orders != null) {
                orderIterator = orders.iterator();
                currOrder = orderIterator.next();
            }

            int i = -1;
            // populating the series with data
            for (Price price: prices) {
                boolean endSearch = false;
                while (!endSearch) {
                    i++;
                    //if an order is placed at this price
                    if (currOrder != null && currOrder.getOrderDate().equals(price.getDate())) {
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
                        XYChart.Data p = new XYChart.Data<Long, Number>(price.getDate().getMillis(), price.getOpen(),
                                new CandleStickChart.CandleStickExtraValues(type,
                                        price.getValue(),
                                        price.getHigh(),
                                        price.getLow(),
                                        price.getValue()));
                        priceChart.getData().add(p);
                        volumeChart.getData().add(volume);
                        //if no order is placed at this price
                    } else if (currOrder == null || currOrder.getOrderDate().isAfter(price.getDate())) {
                        XYChart.Data p = new XYChart.Data<Long, Number>(price.getDate().getMillis(), price.getOpen(),
                                new CandleStickChart.CandleStickExtraValues(NodeType.Price,
                                        price.getValue(),
                                        price.getHigh(),
                                        price.getLow(),
                                        price.getValue()));
                        priceChart.getData().add(p);
                        if (i == 0 || i == prices.size() - 1) {
                            XYChart.Data volume = new XYChart.Data<Long, Number>(price.getDate().getMillis(), 0,
                                    new XYBarChart.XYBarExtraValues(NodeType.Price));
                            volumeChart.getData().add(volume);
                        }
                    } else if (orderIterator != null && orderIterator.hasNext()) {
                        currOrder = orderIterator.next();
                        continue;
                    }
                    endSearch = true;
                }
            }
            barChart.getData().add(volumeChart);
            ObservableList<XYChart.Series<Long, Number>> data = lineChart.getData();
            if (data == null) {
                lineChart.setData(FXCollections.observableArrayList(priceChart));
                syncGraphZooming(); //sync graph zooming the first time data is set
            } else {
                lineChart.getData().add(priceChart);
            }
            lineChart.getXAxis().setAutoRanging(true);
            lineChart.getYAxis().setAutoRanging(true);
            //if different price readers it means the list of companies may differ
            if (priceReader != this.priceReader || orderReader != this.orderReader) {
                this.priceReader = priceReader;
                this.orderReader = orderReader;
                updateCompanyList();
            }
            buildTable(prices, orderSummary);
        } else {
            this.priceReader = priceReader;
            this.orderReader = orderReader;
        }
    }

    private void init(BorderPane graph) {
        DateValueAxis xAxis = new DateValueAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        xAxis.setMinorTickVisible(false);
        yAxis.setLabel("Price");
        lineChart = new CandleStickChart(xAxis, yAxis);
        lineChart.getStyleClass().add("graph");
        yAxis.setForceZeroInRange(false);
        lineChart.setCacheHint(CacheHint.SPEED);
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);

        DateValueAxis xAxisVolume = new DateValueAxis();
        NumberAxis yAxisVolume = new NumberAxis();
        xAxisVolume.setLabel("Date");
        xAxisVolume.setMinorTickVisible(false);
        yAxisVolume.setLabel("Order Volume");
        yAxisVolume.setForceZeroInRange(false);
        barChart = new XYBarChart(xAxisVolume, yAxisVolume);
        barChart.getStyleClass().add("graph");
        barChart.setCacheHint(CacheHint.SPEED);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setPrefHeight(200);

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
        pane.setCenter(ChartPanZoomManager.setup(lineChart));
        pane.setBottom(ChartPanZoomManager.setup(barChart));

        final VBox tableBox = new VBox();
        tableBox.setPadding(new javafx.geometry.Insets(0, 30, 30, 30));
        table = new BorderPane();
        TitleBox pricesBox = new TitleBox("Prices & Orders",table);
        tableBox.getChildren().add(pricesBox);
        VBox.setVgrow(table,Priority.ALWAYS);
        VBox.setVgrow(pricesBox,Priority.ALWAYS);

        graph.setCenter(pane);
        graph.setRight(tableBox);
        graph.setTop(buildFilterSelector());

        addMenu();
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

    private void buildTable(List<Price> prices, Map<DateTime,OrderType> orders) {
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

        table.setCenter(tableView);
        table.setTop(createToolBar(filterableData, orders));
    }

    private ToolBar createToolBar(FilteredList<Price> filterableData, Map<DateTime,OrderType> orders) {
        ToolBar toolbar = new ToolBar();
        toolbar.setId("prices-table-filters");
        final ToggleGroup group = new ToggleGroup();
        ToggleButton filterBuys = new ToggleButton("Buy", ImageUtils.getImage("icons/buy.png"));
        filterBuys.getStyleClass().add("toggle");
        filterBuys.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (filterableData != null)
                    filterableData.setPredicate(price -> orders.containsKey(price.getDate()) && orders.get(price.getDate()).equals(OrderType.BUY));
            }
        });

        ToggleButton filterSells = new ToggleButton("Sell", ImageUtils.getImage("icons/sell.png"));
        filterSells.getStyleClass().add("toggle");
        filterSells.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (filterableData != null)
                    filterableData.setPredicate(price -> orders.containsKey(price.getDate()) && orders.get(price.getDate()).equals(OrderType.SELL));
            }
        });

        ToggleButton showAll = new ToggleButton("All", ImageUtils.getImage("icons/all.png"));
        showAll.getStyleClass().add("toggle");
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

    private HBox buildFilterSelector() {
        HBox selector = new HBox();
        selector.getStyleClass().add("selector-panel");
        addFilter("Company", selector);
        addProfileButton(selector);
        DateRangeFilterBuilder.addDateFilters(selector, lineChart);

        return selector;
    }

    private void addFilter(String name, HBox selector) {
        companySelector = new ComboBox<>();
        LabelledSelector filter = new LabelledSelector(name + ":", companySelector);
        new AutoCompleteComboBoxListener<>(companySelector);
        selector.getChildren().add(filter);
    }

    private void addProfileButton(HBox selector) {
        profile = new Button("Profile", ImageUtils.getImage("icons/profile.png"));
        profile.setGraphicTextGap(5);
        selector.getChildren().add(profile);
    }

    private void addCompanySelectorListener() {
        new AutoCompleteComboBoxListener<>(companySelector);
        if (companyListener == null) {
            companyListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String old, String companyName) {
                    //user may type in invalid company name so we have to check whether or not
                    //a valid company name has been selected. Also, don't reload if the selected company
                    //is the same as the previously selected one.
                    if (companyName != null && !companyName.equals(old) && priceReader.getCompanyHistory(companyName) != null) {
                        List<Price> prices = priceReader.getCompanyHistory(companyName);
                        List<Order> orders = orderReader.getCompanyHistory(companyName);
                        profile.setOnAction(DialogBuilder.constructExportableDialog("Profile of " + companyName,
                                constructProfileGraph(companyName)));
                        Map<DateTime, OrderType> orderSummary = new HashMap<>();
                        if (orders != null) {
                            for (Order order : orders) {
                                orderSummary.put(order.getOrderDate(), order.getOrderType());
                            }
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                buildGraph(graph, prices, orders, priceReader, orderReader, orderSummary);
                            }
                        });
                    }
                }
            };
        }
        companySelector.valueProperty().addListener(companyListener);
    }

    private void updateCompanyList() {
        //update list of companies in the selector
        if (companyListener != null) {
            companySelector.valueProperty().removeListener(companyListener);
        }
        companySelector.getSelectionModel().clearSelection();
        companySelector.setItems(FXCollections.observableArrayList(priceReader.getHistory().getAllCompanies()));
        companySelector.getSelectionModel().selectFirst();
        profile.setOnAction(DialogBuilder.constructExportableDialog("Profile for " + companySelector.getSelectionModel().getSelectedItem(),
                constructProfileGraph(companySelector.getSelectionModel().getSelectedItem())));
        addCompanySelectorListener();
    }

    private List<Node> constructProfileGraph(String company) {
        List<Node> content = new ArrayList<>();
        ProfileBuilder profileBuilder = new ProfileBuilder();
        content.add(profileBuilder.buildProfile(new Profile(priceReader.getCompanyHistory(company)), Orientation.HORIZONTAL));
        return content;
    }
}
