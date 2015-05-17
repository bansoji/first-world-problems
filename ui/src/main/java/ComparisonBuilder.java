import format.FormatUtils;
import graph.ChartPanZoomManager;
import graph.DateValueAxis;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.CacheHint;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import main.Portfolio;
import main.Profit;

import java.util.*;

/**
 * Created by gavintam on 14/05/15.
 */
public class ComparisonBuilder {

    private LineChart lineChart;
    private BarChart barChart;
    private Map<String,List<Profit>> lines = new HashMap<>();
    private Map<String,XYChart.Series> series = new HashMap<>();
    private List<String> bestStrategies = new ArrayList<>();
    private Map<String,Double> strategyProfits = new HashMap<>();
    private Map<String, Map<String,String>> strategyParams = new HashMap<>();
    private String dataFile;

    private static final int NUM_RESULTS = 5;

    public void buildComparison(StrategyRunner runner, BorderPane comparison, Portfolio portfolio, Map<String,?> params) {
        if (lineChart == null) initGraphs();
        if (lines.containsValue(portfolio.getProfitList())) return;
        if (dataFile == null) {
            dataFile = runner.getDataFile();
        } else if (!dataFile.equals(runner.getDataFile())) {
            clearGraph();
        } //else if same data file then just add the comparison
        addComparison(runner, params, portfolio);
        BorderPane charts = new BorderPane();
        charts.setId("comparison-charts");
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
        barChart.setPrefHeight(300);
    }

    private void addComparison(StrategyRunner runner, Map<String,?> params, Portfolio portfolio) {
        if (runner == null || runner.getDataFile() == null || params == null) return;
        XYChart.Series<Long,Double> series = new XYChart.Series<>();
        for (Profit p: portfolio.getProfitList()) {
            series.getData().add(new XYChart.Data<>(p.getProfitDate().getMillis(), p.getProfitValue()));
        }
        String strategyName = FileUtils.extractFilename(runner.getStrategyFile());
        series.setName(strategyName);
        //only add line for strategy if parameters result in better profit or if we haven't run 5 strategies yet
        if (bestStrategies.size() < NUM_RESULTS || portfolio.getTotalReturnValue() > strategyProfits.get(bestStrategies.get(NUM_RESULTS - 1))) {
            if (this.series.containsKey(strategyName)) {
                if (portfolio.getTotalReturnValue() > strategyProfits.get(strategyName)) {
                    XYChart.Series lessProfitableSeries = this.series.remove(strategyName);
                    lineChart.getData().remove(lessProfitableSeries);
                } else {
                    return;
                }
            }
            lineChart.getData().add(series);
            Tooltip tooltip = new Tooltip(strategyName);
            Tooltip.install(series.getNode(), tooltip);
            lines.put(strategyName,portfolio.getProfitList());
            this.series.put(strategyName,series);

            Map<String, String> copy = new HashMap<>();
            for (String param : params.keySet()) {
                copy.put(param, String.valueOf(params.get(param)));
            }

            updateRankings(strategyName, copy, portfolio);
        }
    }

    private void updateRankings(String strategyName, Map<String, String> params, Portfolio portfolio) {
        //if we have ran less than NUM_RESULTS of strategies, then just add new strategies to the rankings
        if (!bestStrategies.contains(strategyName)) {
            if (bestStrategies.size() >= NUM_RESULTS) {
                String lessProfitableStrategy = bestStrategies.remove(NUM_RESULTS - 1);
                strategyProfits.remove(lessProfitableStrategy);
                strategyParams.remove(lessProfitableStrategy);
            }
            bestStrategies.add(strategyName);
        }
        strategyProfits.put(strategyName, portfolio.getTotalReturnValue());
        strategyParams.put(strategyName, params);
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
            XYChart.Data data = new XYChart.Data<>(String.valueOf(rank++), strategyProfits.get(strategy));
            data.setExtraValue(strategy);
            series.getData().add(data);
        }
        //always shows 5 rankings, even if there is no value yet, just put zero
        for (int i = rank; i <= 5; i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i),(double)0));
        }

        barChart.getData().setAll(series);
        //install tooltips
        for (XYChart.Data data: series.getData()) {
            Tooltip tooltip = new Tooltip();
            String strategy = (String)data.getExtraValue();
            //if we haven't run 5 strategies yet, some data points will not have a strategy attributed to it
            if (strategy == null) continue;
            tooltip.setGraphic(new TooltipContent(strategy, strategyParams.get(strategy), strategyProfits.get(strategy)));
            Tooltip.install(data.getNode(), tooltip);
            if (strategyProfits.get(strategy) > 0) {
                data.getNode().getStyleClass().add("bar-profit");
            } else {
                data.getNode().getStyleClass().add("bar-loss");
            }
        }
    }

    private void clearGraph() {
        lineChart.getData().clear();
        lines.clear();
    }

    private static class TooltipContent extends GridPane {

        private TooltipContent(String strategy, Map<String,String> params, double profit) {
            getStyleClass().add("tooltip-content");

            Label strategyLabel = new Label("STRATEGY:");
            setConstraints(strategyLabel,0,0);
            Label strategyName = new Label(strategy);
            setConstraints(strategyName,0,1);

            Label paramLabel = new Label("PARAMETERS:");
            setConstraints(paramLabel,0,2);
            getChildren().addAll(strategyLabel, strategyName, paramLabel);
            int rowNum = 3;
            for (String param: params.keySet()) {
                Label paramName = new Label(param + ":");
                Label value = new Label(params.get(param));
                setConstraints(paramName,0,rowNum);
                setConstraints(value,1,rowNum);
                GridPane.setHalignment(value, HPos.RIGHT);
                getChildren().addAll(paramName,value);
                rowNum++;
            }
            Label profitLabel = new Label("PROFIT:");
            Label profitValue = new Label(FormatUtils.formatPrice(profit));
            if (profit > 0) {
                profitValue.getStyleClass().add("profit-label");
            } else {
                profitValue.getStyleClass().add("loss-label");
            }
            setConstraints(profitLabel,0,rowNum);
            setConstraints(profitValue,0,rowNum+1);
            getChildren().addAll(profitLabel, profitValue);
        }
    }
}
