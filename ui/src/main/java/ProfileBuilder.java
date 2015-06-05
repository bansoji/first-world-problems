import eu.hansolo.enzo.charts.SimpleRadarChart;
import format.FormatUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import core.Profile;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gavintam on 20/05/15.
 */
public class ProfileBuilder {

    private BarChart barChart;

    public BorderPane buildProfile(Profile profile, Orientation orientation) {
        if (barChart == null) init();
         return buildChart(profile, orientation);
    }

    private void init() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Metric");
        yAxis.setLabel("Rating");
        yAxis.setForceZeroInRange(false);
        barChart = new BarChart(xAxis, yAxis);
        barChart.getStyleClass().add("profile-graph");
        barChart.setPrefHeight(300);
        barChart.setLegendVisible(false);
        xAxis.tickLabelFontProperty().set(Font.font(8));
    }

    private BorderPane buildChart(Profile profile, Orientation orientation) {
        barChart.getData().clear();
        BorderPane content = new BorderPane();
        XYChart.Series<String,Double> series = new XYChart.Series<>();
        for (String metric: profile.getRatedMetrics().keySet()) {
            XYChart.Data<String,Double> data = new XYChart.Data<>(metric,(double)profile.getRatedMetrics().get(metric));
            addBarHoverColourChange(data);
            series.getData().add(data);
        }
        barChart.getData().add(series);
        Map<String,Double> metrics = profile.getMetrics();
        for (XYChart.Data data: series.getData()) {
            String metricName = (String)data.getXValue();
            Tooltip tooltip = new Tooltip(metricName + ":\n" + FormatUtils.round5dp(metrics.get(metricName)));
            Tooltip.install(data.getNode(),tooltip);
        }
        if (orientation.equals(Orientation.HORIZONTAL)) {
            content.setRight(barChart);
        } else {
            content.setBottom(barChart);
        }
        content.setCenter(buildRadarChart(profile));
        return content;
    }

    private void addBarHoverColourChange(XYChart.Data data) {
        data.nodeProperty().addListener(new ChangeListener<Node>() {
            @Override
            public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
                if (newNode != null) {
                    newNode.getStyleClass().add("bar-metrics");
                }
            }
        });
    }

    private SimpleRadarChart buildRadarChart(Profile profile) {
        SimpleRadarChart chart = new SimpleRadarChart();
        chart.setTitle(profile.getCompany());
        chart.setScaleVisible(true);
        chart.setMinValue(0);
        chart.setMaxValue(5);
        chart.setPrefWidth(500);
        chart.setMinWidth(500);
        //chart.setZeroLineVisible(true);
        chart.setFilled(true);
        chart.setNoOfSectors(profile.getRatedMetrics().size());
        List<String> keys = new ArrayList<>(profile.getRatedMetrics().keySet());
        for (int i = 0; i < profile.getMetrics().size(); i++) {
            chart.addData(i, new XYChart.Data<>(keys.get(i), (double)profile.getRatedMetrics().get(keys.get(i))));
        }
        chart.setGradientStops(new Stop(0.00000, Color.web("#3552a0")),
                new Stop(0.09090, Color.web("#456acf")),
                new Stop(0.27272, Color.web("#45a1cf")),
                new Stop(0.36363, Color.web("#30c8c9")),
                new Stop(0.45454, Color.web("#30c9af")),
                new Stop(0.50909, Color.web("#56d483")),
                new Stop(0.72727, Color.web("#9adb49")),
                new Stop(0.81818, Color.web("#efd750")),
                new Stop(0.90909, Color.web("#ef9850")),
                new Stop(1.00000, Color.web("#ef6050")));
        chart.setPolygonMode(true);
        return chart;
    }
}
