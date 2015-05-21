import components.LabeledSelector;
import dialog.DialogBuilder;
import file.FileUtils;
import file.StrategyRunner;
import format.FormatUtils;
import graph.ChartPanZoomManager;
import graph.DateValueAxis;
import image.ImageUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Side;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import core.Portfolio;
import core.Price;
import core.Profit;
import profit.OptimalProfit;

import java.util.*;

/**
 * Created by gavintam on 14/05/15.
 */
public class ComparisonBuilder {

    private TabPane tabPane;

    private TableView strategiesTableView;
    private LineChart strategiesLineChart;
    private BarChart strategiesBarChart;
    private Map<String,List<Profit>> strategiesLines = new HashMap<>();
    private Map<String,XYChart.Series> strategiesSeries = new HashMap<>();
    private List<String> bestStrategies = new ArrayList<>();
    private Map<String,Double> strategyProfits = new HashMap<>();
    private Map<String,Double> strategiesTotalBuyAmount = new HashMap<>();
    private Map<String, Map<String,String>> strategyParams = new HashMap<>();
    private Map<String, Map<String,Object>> strategiesTableRows = new HashMap<>();
    private boolean optimalPlotted;

    private Portfolio portfolio;

    private LineChart companiesLineChart;
    private Button companySelector;
    private Map<String,XYChart.Series> companiesSeries = new HashMap<>();
    private ComboBox<String> modeSelector = new ComboBox<>();

    private String dataFile;
    private String strategyFile;
    private String paramFile;

    private Map<String,Object> prevParams;

    private static final int NUM_RESULTS = 5;

    public void buildComparison(StrategyRunner runner, BorderPane comparison, Portfolio portfolio, List<Price> prices, Map<String,?> params) {
        if (tabPane == null) init();
        this.portfolio = portfolio;
        if (strategiesLines.containsValue(portfolio.getProfitList())) return;
        boolean differentParams = false;
        if (prevParams == null) {
            differentParams = true;
            prevParams = new HashMap<>();
        } else {
            if (params.size() != prevParams.size()) {
                differentParams = true;
            } else {
                for (String paramName : params.keySet()) {
                    //if the previous params do not have this parameter or if the values are different
                    if (!prevParams.containsKey(paramName) || prevParams.get(paramName) != params.get(paramName)) {
                        differentParams = true;
                        break;
                    }
                }
            }
            prevParams.clear();
        }
        for (String paramName: params.keySet()) {
            prevParams.put(paramName, params.get(paramName));
        }
        if (runner.getDataFile() != null && runner.getParamFile() != null && runner.getStrategyFile() != null) {
            if (!runner.getDataFile().equals(dataFile)) {
                clearAllData();
                addStrategyComparison(runner.getStrategyFile(), params, prices);
                updateCompanyComparison();
            } else if (!runner.getStrategyFile().equals(strategyFile) || differentParams) {
                clearCompaniesGraph();
                addStrategyComparison(runner.getStrategyFile(), params, prices);
                updateCompanyComparison();
            }
        }
        //update prev files
        dataFile = runner.getDataFile();
        strategyFile = runner.getStrategyFile();
        paramFile = runner.getParamFile();
        comparison.setCenter(tabPane);
    }

    private void init() {
        constructStrategyTabContent();
        constructCompanyTabContent();

        tabPane = new TabPane();
        BorderPane strategiesContent = new BorderPane();
        BorderPane charts = new BorderPane();
        charts.setId("comparison-charts");
        charts.setCenter(ChartPanZoomManager.setup(strategiesLineChart));
        charts.setBottom(strategiesBarChart);
        strategiesContent.setCenter(charts);
        strategiesContent.setRight(strategiesTableView);
        Tab strategiesTab = new Tab("Strategies");
        strategiesTab.setContent(strategiesContent);
        strategiesTab.setClosable(false);

        Tab companiesTab = new Tab("Companies");
        BorderPane companiesContent = new BorderPane();
        companiesContent.setCenter(ChartPanZoomManager.setup(companiesLineChart));
        companySelector = new Button("Select companies", ImageUtils.getImage("icons/company.png"));
        modeSelector.getItems().addAll("Return Value", "Return %");
        modeSelector.getSelectionModel().selectFirst();
        addModeAction();
        LabeledSelector mode = new LabeledSelector("Mode:", modeSelector);
        HBox topBar = new HBox();
        topBar.getStyleClass().add("selector-panel");
        topBar.getChildren().addAll(companySelector, mode);
        companiesContent.setTop(topBar);
        companiesTab.setContent(companiesContent);
        companiesTab.setClosable(false);
        tabPane.getTabs().addAll(strategiesTab,companiesTab);
        tabPane.setSide(Side.LEFT);
    }

    private void constructStrategyTabContent() {
        DateValueAxis xAxis = new DateValueAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        xAxis.setMinorTickVisible(false);
        yAxis.setLabel("Profit");
        strategiesLineChart = new LineChart(xAxis, yAxis);
        strategiesLineChart.setId("comparison-graph");
        yAxis.setForceZeroInRange(false);
        strategiesLineChart.setCacheHint(CacheHint.SPEED);
        strategiesLineChart.setLegendVisible(false);
        strategiesLineChart.setCreateSymbols(false);
        ChartPanZoomManager.addResetZoomFunction(strategiesLineChart);

        CategoryAxis xAxisBarChart = new CategoryAxis();
        NumberAxis yAxisBarChart = new NumberAxis();
        xAxisBarChart.setLabel("Ranking");
        yAxisBarChart.setLabel("Profit");
        yAxisBarChart.setForceZeroInRange(false);
        strategiesBarChart = new BarChart(xAxisBarChart, yAxisBarChart);
        strategiesBarChart.setId("comparison-bar-graph");
        strategiesBarChart.setLegendVisible(false);
        strategiesBarChart.setPrefHeight(300);

        strategiesTableView = new TableView();
        strategiesTableView.setPlaceholder(new Label("No strategies run."));

        TableColumn strategyCol = new TableColumn("Strategy");
        strategyCol.setMinWidth(100);
        strategyCol.setCellValueFactory(new MapValueFactory<>("Strategy"));

        strategyCol.setCellFactory(new Callback<TableColumn<Map, Object>,
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

        TableColumn returnCol = new TableColumn("Return");
        returnCol.setMinWidth(50);
        returnCol.setCellValueFactory(new MapValueFactory<>("Return"));
        returnCol.setCellFactory(new Callback<TableColumn<Map, Object>,
                TableCell<Map, Object>>() {
            @Override
            public TableCell call(TableColumn p) {
                return new TextFieldTableCell(new DoubleStringConverter());
            }
        });

        TableColumn returnPercentageCol = new TableColumn("Return %");
        returnPercentageCol.setMinWidth(50);
        returnPercentageCol.setCellValueFactory(new MapValueFactory<>("Return %"));
        returnPercentageCol.setCellFactory(new Callback<TableColumn<Map, Object>,
                TableCell<Map, Object>>() {
            @Override
            public TableCell call(TableColumn p) {
                return new TextFieldTableCell(new DoubleStringConverter());
            }
        });

        TableColumn hitRatioCol = new TableColumn("Hit ratio");
        hitRatioCol.setMinWidth(50);
        hitRatioCol.setCellValueFactory(new MapValueFactory<>("Hit ratio"));
        hitRatioCol.setCellFactory(new Callback<TableColumn<Map, Object>,
                TableCell<Map, Object>>() {
            @Override
            public TableCell call(TableColumn p) {
                return new TextFieldTableCell(new DoubleStringConverter());
            }
        });

        strategiesTableView.getColumns().addAll(strategyCol, returnCol, returnPercentageCol, hitRatioCol);
        //ensures extra space to given to existing columns
        strategiesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void constructCompanyTabContent() {
        DateValueAxis xAxis = new DateValueAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        xAxis.setMinorTickVisible(false);
        yAxis.setLabel("Profit");
        companiesLineChart = new LineChart(xAxis, yAxis);
        companiesLineChart.setId("comparison-graph");
        yAxis.setForceZeroInRange(false);
        companiesLineChart.setCacheHint(CacheHint.SPEED);
        companiesLineChart.setLegendVisible(false);
        companiesLineChart.setAnimated(false);  //JAVAFX BUG with clearing series data with create symbols set to false
        companiesLineChart.setCreateSymbols(false);
        ChartPanZoomManager.addResetZoomFunction(companiesLineChart);
    }

    private void addModeAction() {
        modeSelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue v, String oldMode, String newMode) {
                if (newMode.equals("Return Value")) {
                    for (String company: companiesSeries.keySet()) {
                        XYChart.Series series = companiesSeries.get(company);
                        series.getData().clear();
                        for (Profit p : portfolio.getCompanyProfitList(company)) {
                            XYChart.Data data = new XYChart.Data<>(p.getProfitDate().getMillis(), p.getProfitValue());
                            series.getData().add(data);
                        }
                    }
                } else if (newMode.equals("Return %")) {
                    for (String company: companiesSeries.keySet()) {
                        XYChart.Series series = companiesSeries.get(company);
                        series.getData().clear();
                        for (Profit p : portfolio.getCompanyProfitList(company)) {
                            XYChart.Data data = new XYChart.Data<>(p.getProfitDate().getMillis(), p.getReturnPercent());
                            series.getData().add(data);
                        }
                    }
                }
            }
        });
    }

    private void updateCompanyComparison() {
        CheckBox[] companies = new CheckBox[portfolio.getCompanies().size()];
        int i = 0;
        for (String company: portfolio.getCompanies()) {
            CheckBox checkBox = new CheckBox(company);
            checkBox.getStyleClass().add("company-checkbox");
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        XYChart.Series<Long, Double> series = new XYChart.Series<>();
                        for (Profit p : portfolio.getCompanyProfitList(company)) {
                            XYChart.Data data = null;
                            if (modeSelector.getSelectionModel().getSelectedItem().equals("Return Value")) {
                                data = new XYChart.Data<>(p.getProfitDate().getMillis(), p.getProfitValue());
                            } else if (modeSelector.getSelectionModel().getSelectedItem().equals("Return %")) {
                                data = new XYChart.Data<>(p.getProfitDate().getMillis(),
                                        portfolio.getReturns().get(company).getPercent()*100);
                            } else {
                                return; //TODO log error
                            }
                            series.getData().add(data);
                        }
                        companiesLineChart.getData().add(series);
                        companiesSeries.put(company, series);
                        Tooltip tooltip = new Tooltip(company);
                        Tooltip.install(series.getNode(), tooltip);
                    } else {
                        XYChart.Series series = companiesSeries.remove(company);
                        companiesLineChart.getData().remove(series);
                    }
                }
            });
            //auto select the first 5 companies
            if (i < 5) {
                checkBox.setSelected(true);
            }
            companies[i++] = checkBox;
        }
        VBox companyCheckboxes = new VBox();
        companyCheckboxes.getChildren().addAll(companies);
        ScrollPane scrollPane = new ScrollPane(companyCheckboxes);
        List<Node> content = new ArrayList<>();
        Label label = new Label("Select companies to compare:");
        content.add(label);
        content.add(scrollPane);
        companySelector.setOnAction(DialogBuilder.constructSelectionModal("Select companies", content));
    }

    private void addStrategyComparison(String strategyFile, Map<String, ?> params, List<Price> prices) {
        XYChart.Series<Long,Double> series = new XYChart.Series<>();
        for (Profit p: portfolio.getProfitList()) {
            XYChart.Data data = new XYChart.Data<>(p.getProfitDate().getMillis(), p.getProfitValue());
            series.getData().add(data);
        }
        String strategyName = FileUtils.extractFilename(strategyFile);
        series.setName(strategyName);
        //only add line for strategy if parameters result in better profit or if we haven't run 5 strategies yet
        if (bestStrategies.size() < NUM_RESULTS || portfolio.getTotalReturnValue() > strategyProfits.get(bestStrategies.get(NUM_RESULTS - 1))) {
            if (this.strategiesSeries.containsKey(strategyName)) {
                if (portfolio.getTotalReturnValue() > strategyProfits.get(strategyName)) {
                    XYChart.Series lessProfitableSeries = this.strategiesSeries.remove(strategyName);
                    strategiesLineChart.getData().remove(lessProfitableSeries);
                } else {
                    return;
                }
            }
            strategiesLineChart.getData().add(series);
            Tooltip tooltip = new Tooltip(strategyName);
            Tooltip.install(series.getNode(), tooltip);
            strategiesLines.put(strategyName, portfolio.getProfitList());
            this.strategiesSeries.put(strategyName, series);

            Map<String, String> copy = new HashMap<>();
            for (String param : params.keySet()) {
                copy.put(param, String.valueOf(params.get(param)));
            }

            updateStrategyRankings(strategyName, copy, portfolio);
        }
        if (!optimalPlotted) {
            OptimalProfit optimalProfit = new OptimalProfit(prices);
            XYChart.Series<Long, Double> optimal = new XYChart.Series<>();
            for (Profit p : optimalProfit.getProfitList()) {
                XYChart.Data data = new XYChart.Data<>(p.getProfitDate().getMillis(), p.getProfitValue());
                optimal.getData().add(data);
            }
            strategiesLineChart.getData().add(optimal);
            optimalPlotted = true;
        }
    }

    private void updateStrategyRankings(String strategyName, Map<String, String> params, Portfolio portfolio) {
        strategyProfits.put(strategyName, portfolio.getTotalReturnValue());
        strategyParams.put(strategyName, params);
        strategiesTotalBuyAmount.put(strategyName, portfolio.getTotalBuyValue());
        //if we have ran less than NUM_RESULTS of strategies, then just add new strategies to the rankings
        if (!bestStrategies.contains(strategyName)) {
            if (bestStrategies.size() >= NUM_RESULTS) {
                String lessProfitableStrategy = bestStrategies.remove(NUM_RESULTS - 1);
                //strategyProfits.remove(lessProfitableStrategy);
                //strategyParams.remove(lessProfitableStrategy);
            }
            bestStrategies.add(strategyName);
        }
        updateStrategyTable(strategyName);
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

        strategiesBarChart.getData().setAll(series);
        //install tooltips
        for (XYChart.Data data: series.getData()) {
            Tooltip tooltip = new Tooltip();
            String strategy = (String)data.getExtraValue();
            //if we haven't run 5 strategies yet, some data points will not have a strategy attributed to it
            if (strategy == null) continue;
            tooltip.setGraphic(new TooltipContentForStrategies(strategy, strategyParams.get(strategy), strategyProfits.get(strategy)));
            Tooltip.install(data.getNode(), tooltip);
            if (strategyProfits.get(strategy) > 0) {
                data.getNode().getStyleClass().add("bar-profit");
            } else {
                data.getNode().getStyleClass().add("bar-loss");
            }
        }
    }

    private void updateStrategyTable(String strategy) {
        List<Profit> profitList = strategiesLines.get(strategy);
        double prevValue = 0;
        int successfulTrades = 0;
        //true if last point is just at the last price point and not correlated with a trade
        boolean neglectLast = false;
        for (Profit profit: profitList) {
            if (profit.getProfitValue() > prevValue) {
                successfulTrades++;
                prevValue = profit.getProfitValue();
            } else if (profit.getProfitValue() ==  prevValue && profit == profitList.get(profitList.size()-1)) {
                neglectLast = true;
            }
        }

        //first point is always 0 so don't include in calculation and last point is just last price point
        //so neglect if it is not correlated with a trade
        double hitRatio = successfulTrades/(double)(profitList.size()-1-(neglectLast ? 1 : 0));
        if (strategiesTableRows.containsKey(strategy)) {
            strategiesTableView.getItems().remove(strategiesTableRows.get(strategy));
        }
        Map<String, Object> row = new HashMap<>();
        row.put("Strategy", strategy);
        row.put("Return", FormatUtils.round2dp(strategyProfits.get(strategy)));
        row.put("Return %", FormatUtils.round3dp((strategyProfits.get(strategy) / strategiesTotalBuyAmount.get(strategy)) * 100));
        row.put("Hit ratio", FormatUtils.round3dp(hitRatio));
        strategiesTableView.getItems().add(row);
        strategiesTableRows.put(strategy, row);
    }

    private void clearAllData() {
        strategiesLineChart.getData().clear();
        strategiesLines.clear();
        strategiesTableView.getItems().clear();
        strategyParams.clear();
        strategyProfits.clear();
        strategiesBarChart.getData().clear();
        strategiesLines.clear();
        strategiesSeries.clear();
        strategiesTotalBuyAmount.clear();
        strategiesTableRows.clear();
        bestStrategies.clear();
        optimalPlotted = false;
        companiesLineChart.getData().clear();
    }

    private void clearCompaniesGraph() {
        companiesLineChart.getData().clear();
    }

    private class TooltipContentForStrategies extends GridPane {

        private TooltipContentForStrategies(String strategy, Map<String, String> params, double profit) {
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
