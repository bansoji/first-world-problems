/**
 * Created by Gavin Tam on 1/05/15.
 */
import file.StrategyRunner;
import format.FormatChecker;
import format.FormatUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.OrderReader;
import main.Portfolio;
import tablecell.NumericEditableTableCell;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Gavin Tam on 25/03/15.
 */
public class AnalysisBuilder {

    private static final Logger logger = Logger.getLogger("application_log");

    private BarChart barChart;
    private TableView tableView;
    private Set<String> prevParams;
    private HashMap<String,Double> paramCombinations = new HashMap<>();
    private ObservableList<String> bestParams = FXCollections.observableList(new LinkedList<String>());
    private HashMap<String,Map<String,String>> bestParamValues = new HashMap<>();
    private ParameterManager<Number> manager = new ParameterManager();
    private HashMap<String,Number> paramStepValues = new HashMap<>();
    private TableView paramStepTable;

    private StrategyRunner runner;
    private Thread t;

    private static final int NUM_RESULTS = 5;

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

        //restart param storage information
        prevParams = null;
        paramCombinations.clear();
        bestParams.clear();
        bestParamValues.clear();
        initParamTable();
    }

    private void updateChart(String paramCombination, Map<String,String> params, double profit) {
        //if we already have our top 5 results and this one is more profitable than one of them
        if (bestParams.size() >= NUM_RESULTS && paramCombinations.get(bestParams.get(NUM_RESULTS-1)) < profit) {
            String old = bestParams.remove(NUM_RESULTS-1);
            bestParamValues.remove(old);
            int i;
            for (i = 0; i < NUM_RESULTS-1; i++) {
                if (paramCombinations.get(bestParams.get(i)) < profit) {
                    break;
                }
            }
            bestParams.add(i,paramCombination);
            bestParamValues.put(paramCombination,params);
        } else if (bestParams.size() < NUM_RESULTS && !bestParams.contains(paramCombination)) {
            bestParams.add(paramCombination);
            bestParamValues.put(paramCombination, params);
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

    public void buildAnalysis(StrategyRunner runner, BorderPane analysis, Set<String> params)
    {
        //init if required
        if (barChart == null || tableView == null) restart();
        HBox graphs = new HBox();
        HBox.setHgrow(barChart,Priority.ALWAYS);
        graphs.getChildren().addAll(buildControls(), barChart);
        analysis.setCenter(graphs);
        this.runner = runner;
        if (prevParams != params) {
            buildTable(params);
            //new params means we clear all the previously recorded results
            paramCombinations.clear();
            bestParams.clear();
            bestParamValues.clear();
            paramStepValues.clear();
        }
        if (runner.getParamFile() != null) updateParamTable();
        prevParams = params;
        analysis.setBottom(tableView);
    }


    public void addRow(Map<String,?> params, double profit) {
        if (params == null || params.size() == 0) return;
        Map<String,Object> row = new HashMap<>();
        Map<String,String> copy = new HashMap<>();
        String combination = "";
        for (String param: params.keySet()) {
            row.put(param, params.get(param));
            copy.put(param, String.valueOf(params.get(param)));
            combination += params.get(param) + "-";
        }
        row.put("Profit",profit);
        if (!paramCombinations.containsKey(combination)) {
            paramCombinations.put(combination, profit);
            if (!Platform.isFxApplicationThread()) {
                final String paramCombination = combination;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        tableView.getItems().add(row);
                        updateChart(paramCombination, copy, profit);
                    }
                });

            } else {
                tableView.getItems().add(row);
                updateChart(combination, copy, profit);
            }
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

    private ToolBar buildControls() {
        Button playButton = new Button("",new ImageView(getClass().getResource("icons/run.png").toExternalForm()));
        playButton.getStyleClass().add("toolbar-button");
        Button pauseButton = new Button("", new ImageView(getClass().getResource("icons/pause.png").toExternalForm()));
        pauseButton.getStyleClass().add("toolbar-button");
        pauseButton.setDisable(true);
        Button clearButton = new Button("", new ImageView(getClass().getResource("icons/refresh.png").toExternalForm()));
        clearButton.getStyleClass().add("toolbar-button");
        Button settingsButton = new Button("",new ImageView(getClass().getResource("icons/spanner.png").toExternalForm()));
        settingsButton.getStyleClass().add("toolbar-button");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (runner.shouldRun()) {
                    updateParamTable();
                    runner.run(true);
                    OrderReader orderReader = new OrderReader("orders.csv");
                    orderReader.readAll();
                    addRow(manager.getParams(), FormatUtils.round2dp(new Portfolio(orderReader.getHistory(),null,null).getTotalReturnValue()));
                }
            }
        };
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                playButton.setDisable(true);
                pauseButton.setDisable(false);
                clearButton.setDisable(true);
                settingsButton.setDisable(true);
                t = new Thread(r);
                t.start();
            }
        });
        pauseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (t != null) {
                    t.interrupt();
                    runner.stop();
                    playButton.setDisable(false);
                    pauseButton.setDisable(true);
                    clearButton.setDisable(false);
                    settingsButton.setDisable(false);
                }
            }
        });
        settingsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.setTitle("Parameters");
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(null);
                VBox dialogVbox = new VBox();
                dialogVbox.setPadding(new Insets(20));
                dialogVbox.setSpacing(20);
                dialogVbox.setAlignment(Pos.CENTER_RIGHT);
                Button close = new Button("Close");
                close.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dialog.close();
                    }
                });
                dialogVbox.getChildren().addAll(paramStepTable, close);
                Scene dialogScene = new Scene(dialogVbox);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        });
        clearButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tableView.getItems().clear();
                barChart.getData().clear();
                paramCombinations.clear();
                bestParams.clear();
                bestParamValues.clear();
                paramStepValues.clear();
            }
        });

        ToolBar controls = new ToolBar(playButton, pauseButton, clearButton, settingsButton);
        controls.setOrientation(Orientation.VERTICAL);
        return controls;
    }

    private void initParamTable() {

        paramStepTable = new TableView();
        paramStepTable.setEditable(true);
        paramStepTable.setPlaceholder(new Label("No parameters available."));

        TableColumn keyCol = new TableColumn("Key");
        keyCol.setMinWidth(100);
        keyCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry, String> param) {
                String key = (String) param.getValue().getKey();
                return new SimpleStringProperty(key);
            }
        });

        TableColumn valueCol = new TableColumn("Step Value");
        valueCol.setEditable(true);
        valueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<Map.Entry, Number> param) {
                if (param.getValue().getValue() instanceof Integer) {
                    int value = (int) param.getValue().getValue();
                    return new SimpleIntegerProperty(value);
                } else {
                    double value = (double) param.getValue().getValue();
                    return new SimpleDoubleProperty(FormatUtils.round3dp(value));
                }
            }
        });
        valueCol.setCellFactory(
        //A custom cell factory that creates cells that only accept numerical input.
            new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn p) {
                return new NumericEditableTableCell();
            }
        });

        valueCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Map.Entry row = (Map.Entry)event.getRowValue();
                String newValue = String.valueOf(event.getNewValue());

                if (FormatChecker.isInteger(newValue) && manager.getParams().get(row.getKey()) instanceof Integer) {
                    paramStepValues.put((String) row.getKey(), Integer.parseInt(newValue));
                } else if (FormatChecker.isDouble(newValue)) {
                    paramStepValues.put((String) row.getKey(), Double.parseDouble(newValue));
                } else {
                    logger.warning("Invalid parameter step value entered.");
                }
            }
        });

        paramStepTable.getColumns().addAll(keyCol, valueCol);
        //ensures extra space to given to existing columns
        paramStepTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        paramStepTable.setMinWidth(300);

        if (paramStepValues == null) {
            paramStepValues = new HashMap<>();
        } else {
            paramStepValues.clear();
        }
    }

    private void updateParamTable() {
        //remove previously stored parameters
        manager.clear();

        Properties props = manager.getProperties(runner.getParamFile());
        Enumeration properties = props.propertyNames();

        boolean needsUpdate = false;
        while (properties.hasMoreElements()) {
            String key = (String)properties.nextElement();
            //if the value of the property is not numerical, it is not a parameter
            String value = props.getProperty(key);
            if (!FormatChecker.isDouble(value)) continue;
            boolean isInteger = FormatChecker.isInteger(value);
            if (isInteger) {
                if (paramStepValues.get(key) == null)
                    paramStepValues.put(key, 0);
                else
                    needsUpdate = true;
            } else {
                if (paramStepValues.get(key) == null)
                    paramStepValues.put(key,0.);
                else
                    needsUpdate = true;
            }
            if (manager.getParams().get(key) == null) {
                if (isInteger) {
                    manager.put(key, Integer.parseInt(value));
                } else {
                    manager.put(key, Double.parseDouble(value));
                }
            }
            if (!paramStepValues.get(key).equals(0)) {
                if (isInteger)
                    manager.put(key, Integer.parseInt(value) + (int) paramStepValues.get(key));
                else
                    manager.put(key, FormatUtils.round3dp(Double.parseDouble(value) + (double) paramStepValues.get(key)));
            }
        }
        ObservableList<Map.Entry> data = FXCollections.observableArrayList(paramStepValues.entrySet());
        paramStepTable.setItems(data);
        if (needsUpdate) {
            manager.updateParams(runner.getParamFile());
        }
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

