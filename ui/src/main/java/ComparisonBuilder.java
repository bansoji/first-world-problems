import components.DateRangeFilterBuilder;
import components.LabelledSelector;
import core.*;
import dialog.DialogBuilder;
import file.FileUtils;
import file.StrategyRunner;
import format.FormatUtils;
import graph.ChartPanZoomManager;
import graph.DateValueAxis;
import image.ImageUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import profit.OptimalProfit;
import table.ExportableTable;
import table.TableUtils;
import website.ReutersLoader;

import java.util.*;

/**
 * Created by gavintam on 14/05/15.
 */
public class ComparisonBuilder {

    private TabPane tabPane;

    private BorderPane profile;
    private ComboBox<String> profileCompanySelector;

    private ExportableTable strategiesTableView;
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
    private double optimalReturn;

    private Portfolio portfolio;

    private LineChart companiesLineChart;
    private Button companySelector;
    private Button moreInfo;
    private Map<String,XYChart.Series> companiesSeries = new HashMap<>();
    private ComboBox<String> modeSelector = new ComboBox<>();

    private String dataFile;
    private String strategyFile;
    private String paramFile;

    private Map<String,Object> prevParams;

    private static final int NUM_RESULTS = 5;

    public void buildComparison(StrategyRunner runner, BorderPane comparison, Portfolio portfolio, List<Price> prices, Reader priceReader, Map<String,?> params) {
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
                buildProfileChart(priceReader);
            } else if (!runner.getStrategyFile().equals(strategyFile) || differentParams) {
                clearCompaniesGraph();
                addStrategyComparison(runner.getStrategyFile(), params, prices);
                updateCompanyComparison();
                buildProfileChart(priceReader);
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
        HBox filters = new HBox();
        filters.getStyleClass().add("selector-panel");
        DateRangeFilterBuilder.addDateFilters(filters, strategiesLineChart);
        charts.setTop(filters);

        strategiesContent.setCenter(charts);
        strategiesContent.setRight(strategiesTableView);
        Tab strategiesTab = new Tab("Strategies");
        strategiesTab.setContent(strategiesContent);
        strategiesTab.setClosable(false);

        Tab companiesTab = new Tab("Companies");
        BorderPane companiesContent = new BorderPane();
        profile = new BorderPane();
        //profile.setId("company-profile");
        profileCompanySelector = new ComboBox<>();
        HBox selector = new HBox();
        selector.getStyleClass().add("selector-panel");
        moreInfo = new Button("", ImageUtils.getImage("icons/reuters.png"));
        moreInfo.getStyleClass().add("transparent-button");
        selector.getChildren().addAll(new LabelledSelector("Company:", profileCompanySelector), moreInfo);
        profile.setTop(selector);
        companiesContent.setCenter(ChartPanZoomManager.setup(companiesLineChart));
        companiesContent.setRight(profile);
        companySelector = new Button("Select companies", ImageUtils.getImage("icons/company.png"));
        modeSelector.getItems().addAll("Return Value", "Return %");
        modeSelector.getSelectionModel().selectFirst();
        addModeAction();
        HBox topBar = new HBox();
        topBar.getStyleClass().add("selector-panel");
        topBar.getChildren().addAll(companySelector, new LabelledSelector("Mode:", modeSelector));
        DateRangeFilterBuilder.addDateFilters(topBar, companiesLineChart);
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

        strategiesTableView = new ExportableTable();
        strategiesTableView.setMaxWidth(300);   //no more than three columns worth of width
        strategiesTableView.setTableMenuButtonVisible(true);
        strategiesTableView.setPlaceholder(new Label("No strategies run."));

        TableColumn strategyCol = TableUtils.createMapColumn("Strategy", TableUtils.ColumnType.String);
        strategyCol.setMinWidth(100);

        TableColumn returnCol = TableUtils.createMapColumn("Return", TableUtils.ColumnType.Double);
        returnCol.setMinWidth(50);

        TableColumn returnPercentageCol = TableUtils.createMapColumn("Return %", TableUtils.ColumnType.Double);
        returnPercentageCol.setMinWidth(50);

        TableColumn hitRatioCol = TableUtils.createMapColumn("Hit ratio", TableUtils.ColumnType.Double);
        hitRatioCol.setMinWidth(50);

        TableColumn numTradesCol = TableUtils.createMapColumn("Trades", TableUtils.ColumnType.Integer);
        numTradesCol.setMinWidth(50);
        numTradesCol.setVisible(false); //by default, this is not shown

        TableColumn percentageOptimalCol = TableUtils.createMapColumn("% of Optimal Return", TableUtils.ColumnType.Double);
        percentageOptimalCol.setMinWidth(50);
        percentageOptimalCol.setVisible(false);

        strategiesTableView.getColumns().addAll(strategyCol, returnCol, returnPercentageCol, hitRatioCol, numTradesCol, percentageOptimalCol);
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
        ListView<String> listView = new ListView<>();
        for (String company: portfolio.getCompanies()) {
            listView.getItems().add(company);
        }

        listView.setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(String company) {
                BooleanProperty observable = new SimpleBooleanProperty();
                observable.addListener(new ChangeListener<Boolean>() {
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
                return observable;
            }
        }));

        List<Node> content = new ArrayList<>();
        Label label = new Label("Select companies to compare:");
        content.add(label);
        content.add(listView);
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
        if (!optimalPlotted) {
            OptimalProfit optimalProfit = new OptimalProfit(prices);
            XYChart.Series<Long, Double> optimal = new XYChart.Series<>();
            for (Profit p : optimalProfit.getProfitList()) {
                XYChart.Data data = new XYChart.Data<>(p.getProfitDate().getMillis(), p.getProfitValue());
                optimal.getData().add(data);
            }
            optimalReturn = optimalProfit.getProfitList().get(optimalProfit.getProfitList().size()-1).getProfitValue();
            strategiesLineChart.getData().add(optimal);
            Tooltip tooltip = new Tooltip("OPTIMAL");
            Tooltip.install(optimal.getNode(), tooltip);
            optimalPlotted = true;
        }
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
    }

    private void updateStrategyRankings(String strategyName, Map<String, String> params, Portfolio portfolio) {
        strategyProfits.put(strategyName, portfolio.getTotalReturnValue());
        strategyParams.put(strategyName, params);
        strategiesTotalBuyAmount.put(strategyName, portfolio.getTotalBuyValue());
        //if we have ran less than NUM_RESULTS of strategies, then just add new strategies to the rankings
        if (!bestStrategies.contains(strategyName)) {
            if (bestStrategies.size() >= NUM_RESULTS) {
                bestStrategies.remove(NUM_RESULTS - 1);
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

    private void buildProfileChart(Reader priceReader) {
        profileCompanySelector.setItems(FXCollections.observableArrayList(portfolio.getCompanies()));
        profileCompanySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String company = observable.getValue();
                ProfileBuilder profileBuilder = new ProfileBuilder();
                profile.setCenter(profileBuilder.buildProfile(new Profile(priceReader.getCompanyHistory(company)), Orientation.VERTICAL));
                moreInfo.setOnAction(DialogBuilder.constructSimpleDialog(company, ReutersLoader.buildWebsiteDialog(company)));
            }
        });
        profileCompanySelector.getSelectionModel().selectFirst();
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
        int numTrades = profitList.size()-1-(neglectLast ? 1 : 0);
        double hitRatio = successfulTrades/(double)numTrades;
        if (strategiesTableRows.containsKey(strategy)) {
            strategiesTableView.getItems().remove(strategiesTableRows.get(strategy));
        }
        Map<String, Object> row = new HashMap<>();
        row.put("Strategy", strategy);
        row.put("Return", FormatUtils.round2dp(strategyProfits.get(strategy)));
        row.put("Return %", FormatUtils.round3dp((strategyProfits.get(strategy) / strategiesTotalBuyAmount.get(strategy)) * 100));
        row.put("Hit ratio", FormatUtils.round3dp(hitRatio));
        row.put("Trades", numTrades);
        row.put("% of Optimal Return", FormatUtils.round2dp((strategyProfits.get(strategy)/optimalReturn)*100));
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
        optimalReturn = 0;
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
