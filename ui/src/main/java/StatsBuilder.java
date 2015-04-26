import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

import java.util.*;

/**
 * Created by Gavin Tam on 27/03/15.
 */
public class StatsBuilder {

    public static void build(Pane stats, History<Order> history, List<Price> prices, Map<Date, OrderType> orders) {
        final VBox vbox = new VBox();
        vbox.setSpacing(15);
        Portfolio portfolio = new Portfolio(history);
        vbox.getChildren().addAll(buildPortfolioStats(portfolio),buildEquityTable(portfolio.getAssetValue()));

        final HBox hbox = new HBox();
        hbox.setSpacing(50);
        hbox.getChildren().addAll(vbox,buildTable(portfolio.getReturns()),buildReturnChart(portfolio.getReturns()));
        hbox.setPadding(new Insets(50, 30, 50, 30));
        stats.getChildren().setAll(hbox);
    }

    private static VBox buildPortfolioStats(Portfolio portfolio) {

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

        double totalEquityValue = totalPortfolioValue-portfolio.getTotalReturnValue();

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

        VBox stats = new VBox();
        stats.setSpacing(20);

        GridPane totalReturns = new GridPane();
        totalReturns.setHgap(100);
        totalReturns.setId("returns");
        Label totalReturnAmountLabel = new Label("Total returns:");
        totalReturnAmountLabel.getStyleClass().add("sm-label");
        Label totalReturnValue = new Label("$" + round2dp(totalPortfolioValue-totalEquityValue));
        totalReturnValue.getStyleClass().add("lg-label");
        Label percentReturnAmountLabel = new Label("% return:");
        percentReturnAmountLabel.getStyleClass().add("sm-label");
        Label percentReturnValue = new Label(round2dp(portfolio.getTotalReturnValue()/(portfolio.getTotalBuyValue()-totalEquityValue)*100) + "%");
        percentReturnValue.getStyleClass().add("lg-label");
        totalReturns.setConstraints(totalReturnAmountLabel,0,0);
        totalReturns.setConstraints(totalReturnValue,0,1);
        totalReturns.setConstraints(percentReturnAmountLabel,1,0);
        totalReturns.setConstraints(percentReturnValue,1,1);
        totalReturns.getChildren().addAll(totalReturnAmountLabel,totalReturnValue,percentReturnAmountLabel,percentReturnValue);

        stats.getChildren().addAll(totalReturns, barChart);
        return stats;
    }

    /** places a text label with a bar's value above a bar node for a given XYChart.Data */
    private static void displayLabelForData(XYChart.Data<Number, String> data) {
        final Node node = data.getNode();
        final Text dataText = new Text("$" + round2dp(data.getXValue().doubleValue()));
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

    private static TableView buildTable(Map<String,List<Double>> returns) {

        ObservableList<Map> data = FXCollections.observableArrayList();
        for (String company: returns.keySet()) {
            Map<String,Object> row = new HashMap<String,Object>();
            row.put("Company",company);
            row.put("Return", round2dp(returns.get(company).get(0)));
            row.put("Return %", round2dp(returns.get(company).get(0)/returns.get(company).get(2)*100));
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
        return tableView;
    }

    private static PieChart buildReturnChart (Map<String,List<Double>> returns) {
        List<PieChart.Data> returnData = new ArrayList<>();
        for (String company: returns.keySet()) {
            PieChart.Data companyData = new PieChart.Data(company,returns.get(company).get(0));
            Tooltip tooltip = new Tooltip();
            tooltip.setGraphic(new TooltipContent(company,companyData.getPieValue()));
            Tooltip.install(companyData.getNode(), tooltip);
            returnData.add(companyData);
        }
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(returnData);
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Returns by company");
        return chart;
    }

    private static TableView buildEquityTable(Map<String,Double> equities) {
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

    private static class TooltipContent extends GridPane {
        private Label company = new Label();
        private Label returns = new Label();

        private TooltipContent(String companyName, double returnAmount) {
            Label returnLabel = new Label("RETURN: ");
            getStyleClass().add("tooltip-content");

            company.setText(companyName);
            returns.setText(Double.toString(returnAmount));

            setConstraints(company, 0, 0);
            setConstraints(returnLabel,0,1);
            setConstraints(returns,1,1);
            getChildren().addAll(company,returnLabel,returns);
        }
    }

    private static double round2dp (double n) {
        return (double)Math.round(n*100)/100;
    }
}
