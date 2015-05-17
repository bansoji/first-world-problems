import format.FormatUtils;
import graph.ChartPanZoomManager;
import graph.DateValueAxis;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import main.*;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;

/**
 * Created by Gavin Tam on 27/03/15.
 */
public class StatsBuilder {

    private static int MAX_NODES = 200;

    public void build(GridPane stats, Portfolio portfolio) {
        final VBox vbox = new VBox();
        vbox.setSpacing(15);
        TableView equity = buildEquityTable(portfolio.getAssetValue());
        vbox.getChildren().addAll(buildPortfolioStats(portfolio),equity);
        VBox.setVgrow(equity, Priority.ALWAYS);

        stats.setPadding(new Insets(30, 30, 30, 30));

        TableView returnTable = buildTable(portfolio.getReturns(),portfolio.getTotalReturnValue());
        VBox graphs = new VBox();
        vbox.setSpacing(30);
        graphs.getChildren().addAll(buildProfitChart(portfolio.getProfitList()), returnTable);


        stats.setConstraints(vbox, 0, 0);
        stats.setConstraints(graphs, 1, 0);
        stats.setHgap(50);
        GridPane.setVgrow(vbox, Priority.ALWAYS);
        GridPane.setVgrow(graphs, Priority.ALWAYS);
        GridPane.setHgrow(graphs, Priority.ALWAYS);
        stats.getChildren().setAll(vbox,graphs);

        if(!portfolio.getReturns().isEmpty()){
            ////////////////////////
            //TODO Put this somewhere useful!
            ReportGenerator reporter = new ReportGenerator(portfolio);
            reporter.generateReport();
            //////////////////////////////////////////////////
        }
    }

    private VBox buildPortfolioStats(Portfolio portfolio) {

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
        final double total = totalPortfolioValue;
        valuePortfolio.nodeProperty().addListener(new ChangeListener<Node>() {
            @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                if (node != null) {
                    displayLabelForData(valuePortfolio);
                    valuePortfolio.getNode().getStyleClass().add(total > 0 ? "bar-positive" : "bar-negative");
                }
            }
        });
        portfolioStats.getData().add(valuePortfolio);

        double totalEquityValue = totalPortfolioValue-portfolio.getTotalReturnValue();

        XYChart.Data valueEquity = new XYChart.Data<Number,String>(totalEquityValue, "Equity");
        valueEquity.nodeProperty().addListener(new ChangeListener<Node>() {
            @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                if (node != null) {
                    displayLabelForData(valueEquity);
                    valueEquity.getNode().getStyleClass().add(totalEquityValue > 0 ? "bar-positive" : "bar-negative");
                }
            }
        });
        portfolioStats.getData().add(valueEquity);

        barChart.getData().add(portfolioStats);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(150);
        barChart.setPrefWidth(500);

        VBox stats = new VBox();
        stats.setSpacing(20);

        GridPane totalReturns = new GridPane();
        totalReturns.setHgap(100);
        totalReturns.setId("returns");

        Label totalReturnAmountLabel = new Label("Total returns:");
        totalReturnAmountLabel.getStyleClass().add("sm-label");
        double totalReturnValue = totalPortfolioValue-totalEquityValue;

        Label totalReturnValueLabel = new Label(FormatUtils.formatPrice(totalReturnValue), getArrowIcon(totalReturnValue));
        totalReturnValueLabel.getStyleClass().add("lg-label");

        Label percentReturnAmountLabel = new Label("% return:");
        percentReturnAmountLabel.getStyleClass().add("sm-label");

        double percentReturnValue = FormatUtils.round2dp(portfolio.getTotalReturnValue() / (portfolio.getTotalBuyValue() - totalEquityValue) * 100);
        Label percentReturnValueLabel = new Label(percentReturnValue + "%", getArrowIcon(percentReturnValue));
        percentReturnValueLabel.getStyleClass().add("lg-label");

        totalReturns.setConstraints(totalReturnAmountLabel,0,0);
        totalReturns.setConstraints(totalReturnValueLabel,0,1);
        totalReturns.setConstraints(percentReturnAmountLabel,1,0);
        totalReturns.setConstraints(percentReturnValueLabel,1,1);
        totalReturns.getChildren().addAll(totalReturnAmountLabel,totalReturnValueLabel,percentReturnAmountLabel,percentReturnValueLabel);

        stats.getChildren().addAll(totalReturns, barChart);
        return stats;
    }

    /** places a text label with a bar's value above a bar node for a given XYChart.Data */
    private void displayLabelForData(XYChart.Data<Number, String> data) {
        final Node node = data.getNode();
        final Text dataText = new Text(FormatUtils.formatPrice(data.getXValue().doubleValue()));
        node.parentProperty().addListener(new ChangeListener<Parent>() {
            @Override public void changed(ObservableValue<? extends Parent> ov, Parent oldParent, Parent parent) {
                Group parentGroup = (Group) parent;
                parentGroup.getChildren().add(dataText);
            }
        });

        node.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
            @Override public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
                dataText.setLayoutX(
                        Math.round(bounds.getMinX() + 10)
                );
                dataText.setLayoutY(
                        Math.round(bounds.getMaxY() - dataText.prefHeight(-1) * 0.5)
                );
            }
        });
    }

    private ImageView getArrowIcon(double returnValue) {
        if (returnValue > 0) {
            return new ImageView(getClass().getResource("icons/up.png").toExternalForm());
        } else {
            return new ImageView(getClass().getResource("icons/down.png").toExternalForm());
        }
    }

    private TableView buildTable(Map<String,List<Double>> returns, double totalReturnValue) {

        ObservableList<Map> data = FXCollections.observableArrayList();
        for (String company: returns.keySet()) {
            Map<String,Object> row = new HashMap<String,Object>();
            row.put("Company",company);
            row.put("Return", FormatUtils.round2dp(returns.get(company).get(0)));
            row.put("Return %", FormatUtils.round2dp(returns.get(company).get(0) / returns.get(company).get(2) * 100));
            data.add(row);
        }

        TableView tableView = new TableView(data);
        tableView.setPlaceholder(new Label("No orders made."));

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
        tableView.setMinWidth(300);

        final MenuItem chartReturnsItem = new MenuItem("Chart", new ImageView(getClass().getResource("icons/graphs_pie.png").toExternalForm()));
        chartReturnsItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.setTitle("Returns by Company");
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(null);
                VBox dialogVbox = new VBox();
                dialogVbox.setPadding(new Insets(20));
                dialogVbox.setSpacing(20);
                dialogVbox.setAlignment(Pos.CENTER);
                Button close = new Button("Close");
                close.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dialog.close();
                    }
                });
                dialogVbox.getChildren().addAll(buildReturnChart(returns,totalReturnValue), close);
                Scene dialogScene = new Scene(dialogVbox);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        });
        final ContextMenu menu = new ContextMenu(chartReturnsItem);
        //tableView.setContextMenu(menu);
        installMenuOptions(tableView,menu);
        return tableView;
    }

    private Node buildReturnChart (Map<String,List<Double>> returns, double totalReturnValue) {
        List<PieChart.Data> returnData = new ArrayList<>();
        if (totalReturnValue >= 0) {
            for (String company : returns.keySet()) {
                if (returns.get(company).get(0).doubleValue() >= 0) {
                    PieChart.Data companyData = new PieChart.Data(company, returns.get(company).get(0));
                    returnData.add(companyData);
                }
            }
        } else {
            VBox placeholder = new VBox();
            placeholder.setId("piechart-placeholder");
            Label title = new Label("Returns by company");
            title.setId("placeholder-title");
            Label placeholderText = new Label("No returns");
            placeholder.getChildren().addAll(title, placeholderText);
            VBox.setVgrow(placeholder,Priority.ALWAYS);
            return placeholder;
        }
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(returnData);
        final PieChart chart = new PieChart(pieChartData);
        //tooltip installation only works after pie chart is set up
        for (PieChart.Data data: pieChartData) {
            Tooltip tooltip = new Tooltip();
            tooltip.setGraphic(new TooltipContent(data.getName(),data.getPieValue()));
            Tooltip.install(data.getNode(), tooltip);
        }
        chart.setLegendVisible(false);
        chart.setTitle("Returns by company");
        return chart;
    }

    private Region buildProfitChart (List<Profit> profitList) {
        DateValueAxis xAxis = new DateValueAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        xAxis.setMinorTickVisible(false);
        yAxis.setLabel("Profit");
        LineChart lineChart = new LineChart(xAxis, yAxis);
        lineChart.setId("profit-graph");
        yAxis.setForceZeroInRange(false);
        lineChart.setCacheHint(CacheHint.SPEED);

        XYChart.Series<Long,Double> series = new XYChart.Series<>();
        Iterator<Profit> i = profitList.iterator();
        int step = Math.max(profitList.size()/MAX_NODES,1);

        int j = 0;
        while (i.hasNext()) {
            Profit p = i.next();
            //add node if it's every (step)th node or if last node
            if (j % step == 0 || !i.hasNext()) {
                series.getData().add(new XYChart.Data<>(p.getProfitDate().getMillis(), p.getProfitValue()));
            }
            j++;
        }
        lineChart.getData().add(series);
        lineChart.setLegendVisible(false);

        DateTimeFormatter df = DateTimeFormat.forPattern("dd-MMM-yyyy");
        for (XYChart.Data data : series.getData()) {
            Tooltip tooltip = new Tooltip();
            tooltip.setGraphic(new TooltipForProfitGraph(df.print((long) data.getXValue()), (double) data.getYValue()));
            Tooltip.install(data.getNode(), tooltip);
        }
        final MenuItem resetZoomItem = new MenuItem("Reset zoom", new ImageView(getClass().getResource("icons/reset_zoom.png").toExternalForm()));
        resetZoomItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                xAxis.setAutoRanging(true);
                yAxis.setAutoRanging(true);
            }
        });
        final ContextMenu menu = new ContextMenu(resetZoomItem);
        lineChart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    xAxis.setAutoRanging(true);
                    yAxis.setAutoRanging(true);
                } else if (event.getButton().equals(MouseButton.MIDDLE)
                        || (event.isShiftDown() && event.getButton().equals(MouseButton.PRIMARY))) {
                    if (menu.isShowing()) {
                        menu.hide();
                    } else {
                        menu.show(lineChart, event.getScreenX(), event.getScreenY());
                    }
                }
            }
        });

        return ChartPanZoomManager.setup(lineChart);
    }

    private TableView buildEquityTable(Map<String,Double> equities) {
        ObservableList<Map> data = FXCollections.observableArrayList();
        for (String company: equities.keySet()) {
            Map<String,Object> row = new HashMap<String,Object>();
            row.put("Company",company);
            row.put("Equity Value", equities.get(company));
            data.add(row);
        }

        TableView tableView = new TableView(data);
        tableView.setPlaceholder(new Label("No securities held."));

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

        TableColumn equityCol = new TableColumn("Equity Value");
        equityCol.setMinWidth(100);
        equityCol.setCellValueFactory(new MapValueFactory<>("Equity Value"));
        equityCol.setCellFactory(new Callback<TableColumn<Map, Object>,
                TableCell<Map, Object>>() {
            @Override
            public TableCell call(TableColumn p) {
                return new TextFieldTableCell(new DoubleStringConverter());
            }
        });

        tableView.getColumns().addAll(companyCol, equityCol);
        //ensures extra space to given to existing columns
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(150);
        return tableView;
    }

    private void installMenuOptions(TableView tableView, ContextMenu menu) {
        tableView.setTableMenuButtonVisible(true);
        // *Register event filter to show or hide the custom show/hide context menu*
        final Node showHideColumnsButton = tableView.lookup(".show-hide-columns-button");
        if (showHideColumnsButton != null) {
            showHideColumnsButton.addEventFilter(MouseEvent.MOUSE_PRESSED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            if (menu.isShowing()) {
                                menu.hide();
                            } else {
                                menu.show(showHideColumnsButton, Side.BOTTOM, 0, 0);
                            }
                            event.consume();
                        }
                    });
        }
    }

    private class TooltipContent extends GridPane {
        private Label company = new Label();
        private Label returns = new Label();

        private TooltipContent(String companyName, double returnAmount) {
            Label returnLabel = new Label("RETURN: ");
            getStyleClass().add("tooltip-content");

            company.setText(companyName);
            returns.setText(FormatUtils.formatPrice(returnAmount));

            setConstraints(company, 0, 0);
            setConstraints(returnLabel,0,1);
            setConstraints(returns,1,1);
            getChildren().addAll(company,returnLabel,returns);
        }
    }

    private class TooltipForProfitGraph extends GridPane {
        private Label dateValue = new Label();
        private Label profitValue = new Label();

        private TooltipForProfitGraph(String date, double profit) {
            getStyleClass().add("tooltip-content");

            Label dateLabel = new Label("DATE:");
            Label profitLabel = new Label("PROFIT:");

            dateValue.setText(date);
            profitValue.setText(FormatUtils.formatPrice(profit));
            if (profit >= 0) {
                profitValue.getStyleClass().add("profit-label");
            } else {
                profitValue.getStyleClass().add("loss-label");
            }

            setConstraints(dateLabel, 0, 0);
            setConstraints(dateValue,1,0);
            setConstraints(profitLabel,0,1);
            setConstraints(profitValue,1,1);
            getChildren().addAll(dateLabel,dateValue,profitLabel,profitValue);
            GridPane.setHalignment(profitValue, HPos.RIGHT);
            GridPane.setHalignment(dateValue, HPos.RIGHT);
        }
    }
}
