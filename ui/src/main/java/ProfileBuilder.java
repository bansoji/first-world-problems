import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import core.Profile;

/**
 * Created by gavintam on 20/05/15.
 */
public class ProfileBuilder {

    private BarChart barChart;

    public BorderPane buildProfile(Profile profile) {
        if (barChart == null) init();
         return buildChart(profile);
    }

    private void init() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Metric");
        yAxis.setLabel("Value");
        yAxis.setForceZeroInRange(false);
        barChart = new BarChart(xAxis, yAxis);
        barChart.getStyleClass().add("profile-graph");
        barChart.setLegendVisible(false);
    }

    private BorderPane buildChart(Profile profile) {
        barChart.getData().clear();
        BorderPane content = new BorderPane();
        XYChart.Series<String,Double> series = new XYChart.Series<>();
        for (String metric: profile.getMetrics().keySet()) {
            XYChart.Data<String,Double> data = new XYChart.Data<>(metric,profile.getMetrics().get(metric));
            series.getData().add(data);
        }
        barChart.getData().add(series);
        for (XYChart.Data data: series.getData()) {

            //TODO
            Tooltip tooltip = new Tooltip();
            Tooltip.install(data.getNode(),tooltip);
        }
        content.setCenter(barChart);
        return content;
    }
}
