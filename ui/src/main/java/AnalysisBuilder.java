/**
 * Created by Gavin Tam on 1/05/15.
 */
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.*;

/**
 * Created by Gavin Tam on 25/03/15.
 */
public class AnalysisBuilder {

    private BarChart barChart;
    private TableView tableView;
    private Set<String> prevParams;
    //identifier for param combinations
    private static int combination_ID = 0;
    private HashMap<String,Double> paramCombinations = new HashMap<>();
    private ObservableList<String> bestParams = FXCollections.observableList(new LinkedList<String>());
    private HashMap<String,Map<String,String>> bestParamValues = new HashMap<>();

    public void restart() {

        CategoryAxis xAxisParam = new CategoryAxis();
        NumberAxis yAxisParam = new NumberAxis();
        xAxisParam.setLabel("Ranking");
        yAxisParam.setLabel("Profit");
        yAxisParam.setForceZeroInRange(false);
        barChart = new BarChart(xAxisParam, yAxisParam);
        barChart.getStyleClass().add("analysis-graph");
        barChart.setLegendVisible(false);
        tableView = new TableView();
    }

    private void updateChart(String paramCombination, Map<String,String> params, double profit) {
        //if we already have our top 5 results and this one is more profitable than one of them
        if (bestParams.size() >= 5 && paramCombinations.get(bestParams.get(4)) < profit) {
            String old = bestParams.remove(4);
            bestParamValues.remove(old);
            int i;
            for (i = 0; i < 4; i++) {
                if (paramCombinations.get(bestParams.get(i)) < profit) {
                    break;
                }
            }
            bestParams.add(i,paramCombination);
            bestParamValues.put(paramCombination,params);
        } else if (bestParams.size() < 5) {
            bestParams.add(paramCombination);
            bestParamValues.put(paramCombination,params);
        } else {
            return;
        }
        bestParams.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (paramCombinations.get(o1) < paramCombinations.get(o2)) return 1;
                if (paramCombinations.get(o1) > paramCombinations.get(o2)) return -1;
                return 0;
            }
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                XYChart.Series<String, Double> series = new XYChart.Series<>();
                int rank = 1;
                for (String combinationId : bestParams) {
                    XYChart.Data data = new XYChart.Data<>(String.valueOf(rank++), paramCombinations.get(combinationId));
                    data.setExtraValue(combinationId);
                    series.getData().add(data);
                }
                //always shows 5 rankings, even if there is no value yet, just put zero
                for (int i = rank; i <= 5; i++) {
                    XYChart.Data data = new XYChart.Data<>(String.valueOf(i),(double)0);
                    series.getData().add(data);
                }
                barChart.getData().setAll(series);
                for (XYChart.Data data : series.getData()) {
                    if ((double)data.getYValue() == 0) continue;
                    String combinationId = (String) data.getExtraValue();
                    Tooltip tooltip = new Tooltip();
                    tooltip.setGraphic(new TooltipContent(bestParamValues.get(combinationId), paramCombinations.get(combinationId)));
                    Tooltip.install(data.getNode(), tooltip);
                    if (paramCombinations.get(combinationId) > 0) {
                        data.getNode().getStyleClass().add("bar-profit");
                    } else {
                        data.getNode().getStyleClass().add("bar-loss");
                    }
                }
            }
        });
    }

    public void buildAnalysis(BorderPane analysis, Set<String> params)
    {
        //init if required
        if (barChart == null || tableView == null) restart();
        HBox graphs = new HBox();
        HBox.setHgrow(barChart,Priority.ALWAYS);
        graphs.setSpacing(50);
        graphs.getChildren().addAll(barChart);
        analysis.setCenter(graphs);
        if (prevParams != params) {
            buildTable(params);
            //new params means we clear all the previously recorded results
            paramCombinations.clear();
        }
        prevParams = params;
        analysis.setBottom(tableView);
    }


    public void addRow(Map<String,String> params, double profit) {
        Map<String,Object> row = new HashMap<>();
        Map<String,String> copy = new HashMap<>();
        String combination = String.valueOf(combination_ID++);
        for (String param: params.keySet()) {
            row.put(param, params.get(param));
            copy.put(param, params.get(param));
        }
        row.put("Profit",profit);
        if (!paramCombinations.containsKey(combination)) {
            paramCombinations.put(combination, profit);
            tableView.getItems().add(row);
            final String paramCombination = combination;
            updateChart(paramCombination, copy, profit);
        }
    }

    private void buildTable(Set<String> params) {
        tableView.getItems().clear();

        for (String param: params) {
            TableColumn col = new TableColumn(param);
            col.setMinWidth(100);
            col.setCellValueFactory(new MapValueFactory<>(param));
            tableView.getColumns().add(col);
        }

        TableColumn profitCol = new TableColumn("Profit");
        profitCol.setMinWidth(100);
        profitCol.setCellValueFactory(new MapValueFactory<>("Profit"));
        tableView.getColumns().add(profitCol);

        ObservableList<Map> data = FXCollections.observableArrayList();
        tableView.setItems(data);
        //ensures extra space to given to existing columns
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private static class TooltipContent extends GridPane {

        private TooltipContent(Map<String,String> params, double profit) {
            getStyleClass().add("tooltip-content");

            Label paramLabel = new Label("PARAMETERS:");
            setConstraints(paramLabel,0,0);
            getChildren().add(paramLabel);
            int rowNum = 1;
            for (String param: params.keySet()) {
                Label paramName = new Label(param + ":");
                Label value = new Label(params.get(param));
                setConstraints(paramName,0,rowNum);
                setConstraints(value,1,rowNum);
                getChildren().addAll(paramName,value);
                rowNum++;
            }
            Label profitLabel = new Label("PROFIT:");
            Label profitValue = new Label(String.valueOf(profit));
            setConstraints(profitLabel,0,rowNum);
            setConstraints(profitValue,0,rowNum+1);
            getChildren().addAll(profitLabel, profitValue);
        }
    }
}

