import graph.ChartPanZoomManager;
import graph.DateValueAxis;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.chart.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import main.Portfolio;
import main.Profit;

import java.util.*;

/**
 * Created by gavintam on 14/05/15.
 */
public class ComparisonBuilder {

    private LineChart lineChart;
    private BarChart barChart;
    private Set<List<Profit>> lines = new HashSet<>();
    private List<String> bestStrategies = new ArrayList<>();
    private Map<String,Double> strategyProfits = new HashMap<>();
    private String dataFile;

    private static final int NUM_RESULTS = 5;

    public void buildComparison(StrategyRunner runner, BorderPane comparison, Portfolio portfolio) {
        if (lineChart == null) initGraphs();
        if (lines.contains(portfolio.getProfitList())) return;
        if (dataFile == null) {
            dataFile = runner.getDataFile();
        } else if (!dataFile.equals(runner.getDataFile())) {
            clearGraph();
        } //else if same data file then just add the comparison
        addComparison(runner, portfolio);
        BorderPane charts = new BorderPane();
        charts.setCenter(ChartPanZoomManager.setup(lineChart));
        charts.setBottom(barChart);
        comparison.setCenter(charts);
    }

    private void initGraphs() {
        DateValueAxis xAxis = new DateValueAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        xAxis.setMinorTickVisible(false);
        yAxis.setLabel("Profit");
        lineChart = new LineChart(xAxis, yAxis);
        lineChart.setId("comparison-graph");
        yAxis.setForceZeroInRange(false);
        lineChart.setCacheHint(CacheHint.SPEED);
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(false);
        //reset zoom if left-clicked twice
        lineChart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    xAxis.setAutoRanging(true);
                    yAxis.setAutoRanging(true);
                }
            }
        });
        CategoryAxis xAxisBarChart = new CategoryAxis();
        NumberAxis yAxisBarChart = new NumberAxis();
        xAxisBarChart.setLabel("Ranking");
        yAxisBarChart.setLabel("Profit");
        yAxisBarChart.setForceZeroInRange(false);
        barChart = new BarChart(xAxisBarChart, yAxisBarChart);
        barChart.setId("comparison-bar-graph");
        barChart.setLegendVisible(false);
    }

    private void addComparison(StrategyRunner runner, Portfolio portfolio) {
        if (runner == null || runner.getDataFile() == null) return;
        XYChart.Series<Long,Double> series = new XYChart.Series<>();
        for (Profit p: portfolio.getProfitList()) {
            series.getData().add(new XYChart.Data<>(p.getProfitDate().getMillis(), p.getProfitValue()));
        }
        lineChart.getData().add(series);
        lines.add(portfolio.getProfitList());
        updateRankings(FileUtils.extractFilename(runner.getStrategyFile()), portfolio);
    }

    private void updateRankings(String strategyName, Portfolio portfolio) {
        //if we have ran less than NUM_RESULTS of strategies, then just add new strategies to the rankings
        if (!bestStrategies.contains(strategyName)) {
            if (bestStrategies.size() >= NUM_RESULTS) {
                if (portfolio.getTotalReturnValue() > strategyProfits.get(bestStrategies.get(NUM_RESULTS - 1))) {
                    String lessProfitableStrategy = bestStrategies.remove(NUM_RESULTS - 1);
                    strategyProfits.remove(lessProfitableStrategy);
                } else {
                    return; //this strategy is not as profitable as those run before it
                }
            }
            bestStrategies.add(strategyName);
            strategyProfits.put(strategyName, portfolio.getTotalReturnValue());
        } else if (portfolio.getTotalReturnValue() > strategyProfits.get(strategyName)) {
            strategyProfits.put(strategyName, portfolio.getTotalReturnValue());
        } else {
            return; //this strategy is not as profitable as a previous run
        }
        //sort strategies from most profit to least
        bestStrategies.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (strategyProfits.get(o1) < strategyProfits.get(o2)) return 1;
                if (strategyProfits.get(o1) > strategyProfits.get(o2)) return -1;
                return 0;
            }
        });
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        int rank = 1;
        for (String strategy : bestStrategies) {
            series.getData().add(new XYChart.Data<>(String.valueOf(rank++), strategyProfits.get(strategy)));
        }
        //always shows 5 rankings, even if there is no value yet, just put zero
        for (int i = rank; i <= 5; i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i),(double)0));
        }

        barChart.getData().setAll(series);
    }

    private void clearGraph() {
        lineChart.getData().clear();
        lines.clear();
    }
}
