import graph.CandleStickChart;
import graph.DateValueAxis;
import graph.NodeType;
import graph.XYBarChart;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.gillius.jfxutils.chart.JFXChartUtil;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Gavin Tam on 25/03/15.
 */
public class GraphBuilder {

    private CandleStickChart lineChart;
    private XYBarChart barChart;

    public void buildGraph(JFXPanel graph, List<Price> prices, List<Order> orders)
    {
        DateValueAxis xAxis = new DateValueAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        xAxis.setMinorTickVisible(false);
        yAxis.setLabel("Price");
        lineChart = new CandleStickChart(xAxis, yAxis);
        lineChart.getStyleClass().add("graph");
        yAxis.setForceZeroInRange(false);

        DateValueAxis xAxisVolume = new DateValueAxis();
        NumberAxis yAxisVolume = new NumberAxis();
        xAxisVolume.setLabel("Date");
        xAxisVolume.setMinorTickVisible(false);
        yAxisVolume.setLabel("Volume");
        yAxisVolume.setForceZeroInRange(false);
        barChart = new XYBarChart(xAxisVolume, yAxisVolume);
        barChart.getStyleClass().add("graph");

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
                                volume = new XYChart.Data<Long, Number>(currOrder.getOrderDate().getTime(), currOrder.getVolume(),
                                                new XYBarChart.XYBarExtraValues(type));
                                changeBarColour(volume, "green");
                            } else {
                                type = NodeType.SellOrder;
                                volume = new XYChart.Data<Long, Number>(currOrder.getOrderDate().getTime(), currOrder.getVolume(),
                                        new XYBarChart.XYBarExtraValues(type));
                                changeBarColour(volume, "red");
                            }
                            XYChart.Data price = new XYChart.Data<Long, Number>(prices.get(i).getDate().getTime(), prices.get(i).getValue(),
                                    new CandleStickChart.CandleStickExtraValues(type,
                                            prices.get(i).getValue()+1,
                                            prices.get(i).getValue()+1.2,
                                            prices.get(i).getValue()-1,
                                            prices.get(i).getValue()));
                            priceChart.getData().add(price);
                            volumeChart.getData().add(volume);
                            //if no order is placed at this price
                        } else if (currOrder == null || currOrder.getOrderDate().after(prices.get(i).getDate())) {
                            XYChart.Data price = new XYChart.Data<Long, Number>(prices.get(i).getDate().getTime(), prices.get(i).getValue(),
                                    new CandleStickChart.CandleStickExtraValues(NodeType.Price,
                                            prices.get(i).getValue()+1,
                                            prices.get(i).getValue()+1.2,
                                            prices.get(i).getValue()-1,
                                            prices.get(i).getValue()));
                            priceChart.getData().add(price);
                            if (i == 0 || i == prices.size()-1) {
                                XYChart.Data volume = new XYChart.Data<Long, Number>(prices.get(i).getDate().getTime(), 0,
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
            ObservableList<XYChart.Series<Long,Number>> data = lineChart.getData();
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
        pane.setStyle("-fx-background-color: white");
        pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        xAxis.setAutoRanging(true);
                        yAxis.setAutoRanging(true);
                        xAxisVolume.setAutoRanging(true);
                        yAxisVolume.setAutoRanging(true);
                    }
                }
            }
        });
        syncGraphZooming(lineChart,barChart);
        pane.setCenter(JFXChartUtil.setupZooming(lineChart));
        pane.setBottom(JFXChartUtil.setupZooming(barChart));
        Scene scene = new Scene(pane);
        //NOTE: Remember to add .css to the compiler settings in Intellij
        scene.getStylesheets().add("graph.css");
        graph.setScene(scene);
    }

    private static void changeBarColour (XYChart.Data data, String colour)
    {
        data.nodeProperty().addListener(new ChangeListener<Node>() {
            @Override
            public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + colour);
                }
            }
        });
    }

    private static void syncGraphZooming(CandleStickChart lineChart, XYBarChart barChart)
    {
        syncZooming(lineChart,barChart);
        syncZooming(barChart,lineChart);
    }

    private static void syncZooming(XYChart chart1, XYChart chart2) {
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
}
