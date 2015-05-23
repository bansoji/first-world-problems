import eu.hansolo.enzo.charts.SimpleRadarChart;
import javafx.geometry.Orientation;
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

import java.util.ArrayList;
import java.util.List;

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
        yAxis.setLabel("Value");
        yAxis.setForceZeroInRange(false);
        barChart = new BarChart(xAxis, yAxis);
        barChart.getStyleClass().add("profile-graph");
        barChart.setPrefHeight(300);
        barChart.setLegendVisible(false);
    }

    private BorderPane buildChart(Profile profile, Orientation orientation) {
        barChart.getData().clear();
        BorderPane content = new BorderPane();
        XYChart.Series<String,Double> series = new XYChart.Series<>();
        for (String metric: profile.getMetrics().keySet()) {
            XYChart.Data<String,Double> data = new XYChart.Data<>(metric,profile.getMetrics().get(metric));
            series.getData().add(data);
        }
        barChart.getData().add(series);
        for (XYChart.Data data: series.getData()) {
            Tooltip tooltip = new Tooltip((String)data.getXValue());
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
        chart.setNoOfSectors(profile.getMetrics().size());
        List<String> keys = new ArrayList<>(profile.getMetrics().keySet());
        for (int i = 0; i < profile.getMetrics().size(); i++) {
            chart.addData(i, new XYChart.Data<>(keys.get(i), (double)Profile.ProfileEvaluator.rate(keys.get(i),profile.getMetrics().get(keys.get(i)))));
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
