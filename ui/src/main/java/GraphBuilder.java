import graph.plugins.DateValueAxis;
import graph.plugins.XYBarChart;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.gillius.jfxutils.chart.JFXChartUtil;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Gavin Tam on 25/03/15.
 */
public class GraphBuilder {

    public static void buildGraph(JFXPanel graph, List<Price> prices, List<Order> orders)
    {
        DateValueAxis xAxis = new DateValueAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        xAxis.setMinorTickVisible(false);
        yAxis.setLabel("Price");
        final LineChart<Long, Number> lineChart = new LineChart<>(xAxis, yAxis);
        yAxis.setForceZeroInRange(false);

        DateValueAxis xAxisVolume = new DateValueAxis();
        NumberAxis yAxisVolume = new NumberAxis();
        xAxisVolume.setLabel("Date");
        xAxisVolume.setMinorTickVisible(false);
        yAxisVolume.setLabel("Volume");
        yAxisVolume.setForceZeroInRange(false);
        final XYBarChart<Long, Number> barChart = new XYBarChart<>(xAxisVolume, yAxisVolume);

        if (prices.size() > 0) {
            //lineChart.setTitle("Price of " + prices.get(0).getCompanyName());
            XYChart.Series<Long, Number> priceChart = new XYChart.Series<>();
            // populating the series with data

            Iterator<Order> orderIterator = orders.iterator();
            Order currOrder = orderIterator.hasNext() ? orderIterator.next() : null;
            XYChart.Series<Long, Number> volumeChart = new XYChart.Series<>();

            for (int i = 0; i < prices.size(); i++) {
                XYChart.Data price = new XYChart.Data<Long, Number>(prices.get(i).getDate().getTime(), prices.get(i).getValue());
                priceChart.getData().add(price);
                ORDER_SEARCH:
                {
                    while (true) {
                        //if an order is placed at this price
                        if (currOrder.getOrderDate().equals(prices.get(i).getDate())) {
                            XYChart.Data volume = new XYChart.Data<Long, Number>(currOrder.getOrderDate().getTime(), currOrder.getVolume());
                            if (currOrder.getOrderType().equals(OrderType.BUY)) {
                                price.setNode(new PriceInfoBox(currOrder.getPrice(), currOrder.getOrderDate(), InfoBox.InfoBoxType.BuyOrder));
                                price.getNode().setStyle("-fx-background-color: green, white");
                                changeBarColour(volume, "green");
                                volume.setNode(new VolumeInfoBox(currOrder.getVolume(), currOrder.getOrderDate(), InfoBox.InfoBoxType.BuyOrder));
                            } else {
                                price.setNode(new PriceInfoBox(currOrder.getPrice(), currOrder.getOrderDate(), InfoBox.InfoBoxType.SellOrder));
                                price.getNode().setStyle("-fx-background-color: red, white");
                                changeBarColour(volume, "red");
                                volume.setNode(new VolumeInfoBox(currOrder.getVolume(), currOrder.getOrderDate(), InfoBox.InfoBoxType.SellOrder));
                            }
                            volumeChart.getData().add(volume);
                            //if no order is placed at this price
                        } else if (currOrder.getOrderDate().after(prices.get(i).getDate())) {
                            price.setNode(new PriceInfoBox(prices.get(i).getValue(), prices.get(i).getDate(), InfoBox.InfoBoxType.Price));
                            price.getNode().setStyle("-fx-background-color: #3915AE, white");
                            if (i == 0 || i == prices.size()-1) {
                                XYChart.Data volume = new XYChart.Data<Long, Number>(prices.get(i).getDate().getTime(), 0);
                                volumeChart.getData().add(volume);
                            }
                        } else if (orderIterator.hasNext()) {
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
            lineChart.getData().add(priceChart);
            lineChart.setLegendVisible(false);
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

    private static void syncGraphZooming(LineChart<Long,Number> lineChart, XYBarChart<Long,Number> barChart)
    {
//        ((ValueAxis)lineChart.getXAxis()).property.addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                barChart.getXAxis().setAutoRanging(false);
//                ((ValueAxis) barChart.getXAxis()).setLowerBound(((ValueAxis) lineChart.getXAxis()).getLowerBound());
//                ((ValueAxis) barChart.getXAxis()).setUpperBound(((ValueAxis) lineChart.getXAxis()).getUpperBound());
//            }
//        });
    }
}
