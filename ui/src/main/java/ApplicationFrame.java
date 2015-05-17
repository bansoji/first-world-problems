import alert.AlertManager;
import format.FormatUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import main.*;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Gavin Tam on 17/03/15.
 */
public class ApplicationFrame extends Application {

    private static final String LOG_FILE = "application_log";
    private static final Logger logger = Logger.getLogger(LOG_FILE);

    private StrategyRunner runner = new StrategyRunner();

    private Reader orderReader;
    private Reader priceReader;

    private Stage stage;
    private BorderPane main;
    private BorderPane graph;
    private GridPane stats;
    private BorderPane analysis;
    private BorderPane comparison;
    private TabPane tabPane;

    private ComboBox<String> companySelector;
    private ChangeListener companyListener;

    private Loader loader;

    private ParameterManager<String> manager = new ParameterManager();
    private TableView paramTable;
    private HashSet<Tab> loadedTabs = new HashSet<>();
    private Portfolio portfolio = new Portfolio(new History<>(),null,null);

    private GraphBuilder g = new GraphBuilder();
    private AnalysisBuilder a = new AnalysisBuilder();
    private StatsBuilder s = new StatsBuilder();
    private ComparisonBuilder c = new ComparisonBuilder();

    private static String VERSION_NUMBER = "1.1.0";
    private static String APPLICATION_INFO = "Version " + VERSION_NUMBER + "   \u00a9 Group 1";
    private static String FOOTER_MESSAGE = "Get the latest release at our website.";

    private static String OUTPUT_FILE_PATH = "orders.csv";

    @Override
    public void start(Stage primaryStage) throws Exception {
        //primaryStage.setFullScreen(true);
        initLogger();
        primaryStage.setTitle("BuyHard Platform");
        main = new BorderPane();
        Scene scene = new Scene(main);
        scene.getStylesheets().addAll("general.css", "graph.css", "stats.css", "analysis.css", "comparison.css");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(768);
        primaryStage.setMinWidth(1024);
        stage = primaryStage;
        initHeader();
        initBody();
        initFooter();
        //TODO Remove hack - for some reason the graph doesn't load for the first time
        loadContent(new ArrayList<>(), new ArrayList<>(), true);

        graph.setVisible(false);
        stats.setVisible(false);
        analysis.setVisible(false);
        comparison.setVisible(false);

        primaryStage.show();
    }

    private void initLogger() {
        logger.setUseParentHandlers(false);
        try {
            FileHandler handler = new FileHandler(LOG_FILE);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
            logger.addHandler(handler);

            logger.info("====== Buy Hard =========\n" +
                    "Developer Team: Group 1\n" +
                    "APPLICATION NAME: BuyHard-Platform-" + VERSION_NUMBER + ".jar\n" +
                    "APPLICATION VERSION: " + VERSION_NUMBER + "\n" +
                    "LOG FILE: " + LOG_FILE);
        } catch (IOException e) {
            System.err.println("Logger could not be initialised.");
        }
    }

    private void initHeader()
    {
        HBox header = new HBox();
        header.setId("header");
        header.setPrefHeight(100);
        header.setSpacing(50);

        // simple displays ImageView the image as is
        HBox appInfo = new HBox();
        appInfo.setSpacing(50);
        appInfo.setId("app-info");
        ImageView logo = new ImageView(getClass().getResource("logosizes/BuyHard2Logo_Small.png").toExternalForm());
        //Label info = new Label(APPLICATION_INFO);
        appInfo.setAlignment(Pos.CENTER_LEFT);
        appInfo.getChildren().addAll(logo);
        header.getChildren().addAll(appInfo);
        main.setTop(header);

        addFileChoosers(header);
        addSettingsPanel(header);
    }

    private void initBody()
    {
        final VBox body = new VBox();
        graph = new BorderPane();
        stats = new GridPane();
        analysis = new BorderPane();
        comparison = new BorderPane();
        addFilterSelector();

        tabPane = new TabPane();
        Tab tab = new Tab();
        tab.setText("Data");
        tab.setGraphic(new ImageView(getClass().getResource("app-icons/tab-data-icon.png").toExternalForm()));
        tab.setClosable(false);
        tab.setContent(graph);
        addHelpModal(tab,new ImageView(getClass().getResource("images/mouse-graph.jpeg").toExternalForm()));

        Tab statsTab = new Tab();
        statsTab.setText("Portfolio");
        statsTab.setGraphic(new ImageView(getClass().getResource("app-icons/tab-portfolio-icon.png").toExternalForm()));
        statsTab.setClosable(false);
        statsTab.setContent(stats);

        Tab analysisTab = new Tab();
        analysisTab.setText("Analysis");
        analysisTab.setGraphic(new ImageView(getClass().getResource("app-icons/tab-analysis-icon.png").toExternalForm()));
        analysisTab.setClosable(false);
        analysisTab.setContent(analysis);

        Tab comparisonTab = new Tab();
        comparisonTab.setText("Comparison");
        comparisonTab.setGraphic(new ImageView(getClass().getResource("app-icons/tab-comparison-icon.png").toExternalForm()));
        comparisonTab.setClosable(false);
        comparisonTab.setContent(comparison);

        tabPane.getTabs().addAll(tab, statsTab, analysisTab, comparisonTab);
        addTabLoadingAction(tabPane);

        body.getChildren().addAll(tabPane, new Separator());

        main.setCenter(body);
    }

    private void addHelpModal(Tab tab, Node node) {
        final MenuItem helpItem = new MenuItem("Help", new ImageView(getClass().getResource("icons/help.png").toExternalForm()));
        helpItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.setTitle("Tutorial");
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(stage);
                VBox dialogVbox = new VBox();
                dialogVbox.getStyleClass().add("tutorial-modal");
                dialogVbox.setAlignment(Pos.CENTER_RIGHT);
                Button close = new Button("Close");
                close.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dialog.close();
                    }
                });
                dialogVbox.getChildren().add(node);
                Scene dialogScene = new Scene(dialogVbox);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        });
        final ContextMenu menu = new ContextMenu(helpItem);
        tab.setContextMenu(menu);
        tab.getContent().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isControlDown() && event.getButton().equals(MouseButton.PRIMARY)) {
                    if (menu.isShowing()) {
                        menu.hide();
                    } else {
                        menu.show(tab.getContent(), event.getScreenX(), event.getScreenY());
                    }
                }
            }
        });
    }

    private void addTabLoadingAction(TabPane tabPane) {
        tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                if (!oldValue.equals(newValue)) {
                    //select the new tab first cause this is not done by default
                    tabPane.getSelectionModel().select(newValue.intValue());
                    if (orderReader == null || priceReader == null) return;
                    Set<String> companies = priceReader.getHistory().getAllCompanies();
                    String firstCompany = companies.iterator().next();
                    loadContent(priceReader.getCompanyHistory(firstCompany), orderReader.getCompanyHistory(firstCompany), false);
                }
            }
        });
    }

    private void initFooter()
    {
        HBox footerPanel = new HBox();
        footerPanel.setAlignment(Pos.CENTER_LEFT);
        footerPanel.setId("footer");
        HBox footerText = new HBox();
        footerText.setId("footer-text");
        HBox.setHgrow(footerText, Priority.ALWAYS);
        footerText.setAlignment(Pos.CENTER_RIGHT);
        Label appInfo = new Label(APPLICATION_INFO);
        Hyperlink websiteLink = new Hyperlink(FOOTER_MESSAGE);
        websiteLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                getHostServices().showDocument("http://cgi.cse.unsw.edu.au/~awondo/se3011/web/");
            }
        });
        footerText.getChildren().addAll(appInfo, websiteLink);
        loader = new Loader();
        footerPanel.getChildren().addAll(loader,footerText);
        main.setBottom(footerPanel);
    }

    private void addFileChooserListener(final AppFileChooser fileChooser)
    {
        fileChooser.addListener(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser chooser = new FileChooser();
                chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                File file = chooser.showOpenDialog(stage);
                if (file != null)
                {
                    try {
                        if (fileChooser.getButtonId().equals("Choose CSV")) {
                            runner.setDataFile(file.getAbsolutePath());
                            updateParamTable();
                        } else if (fileChooser.getButtonId().equals("Choose strategy")) {
                            runner.setStrategyFile(file.getAbsolutePath());
                            updateParamTable();
                        } else if (fileChooser.getButtonId().equals("Choose parameters")) {
                            runner.setParamFile(file.getAbsolutePath());
                            updateParamTable();
                        } else {
                            return;
                        }
                        fileChooser.setLabelText(file.getName());
                    }
                    catch (Exception ex)
                    {
                        logger.severe(ex.getMessage());
                    }
                }
            }
        });
    }

    private void addCompanySelectorListener() {
        new AutoCompleteComboBoxListener<>(companySelector);
        if (companyListener == null) {
            companyListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String old, String companyName) {
                    //user may type in invalid company name so we have to check whether or not
                    //a valid company name has been selected. Also, don't reload if the selected company
                    //is the same as the previously selected one.
                    if (companyName != null && !companyName.equals(old) && priceReader.getCompanyHistory(companyName) != null) {
                        List<Price> prices = priceReader.getCompanyHistory(companyName);
                        List<Order> orders = orderReader.getCompanyHistory(companyName);
                        loadedTabs.remove(tabPane.getSelectionModel().getSelectedItem());
                        loadContent(prices, orders, false);
                    }
                }
            };
        }
        companySelector.valueProperty().addListener(companyListener);
    }

    private void loadContent(List<Price> prices, List<Order> orders, boolean force)
    {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                boolean loaded = loadedTabs.contains(selectedTab);
                if (loaded && !force) {
                    return;
                }
                loader.setText("Refreshing content...");
                loader.setProgress(0);
                if ((tabPane.getSelectionModel().getSelectedItem().getText().equals("Data") && !loaded) || force) {
                    graph.setVisible(false);
                    Map<DateTime, OrderType> orderRecord = new HashMap<>();
                    if (orders != null) {
                        for (Order order : orders) {
                            orderRecord.put(order.getOrderDate(), order.getOrderType());
                        }
                    }
                    g.buildGraph(graph, prices, orders, orderRecord);
                    graph.setVisible(true);
                }
                if ((tabPane.getSelectionModel().getSelectedItem().getText().equals("Portfolio") && !loaded) || force) {
                    stats.setVisible(false);
                    s.build(stats, portfolio);
                    ////////////////////////
                    //TODO Put this somewhere useful!
                    ReportGenerator reporter = new ReportGenerator(portfolio);
                    reporter.generateReport();
                    //////////////////////////////////////////////////
                    stats.setVisible(true);
                }
                if ((tabPane.getSelectionModel().getSelectedItem().getText().equals("Analysis") && !loaded) || force) {
                    analysis.setVisible(false);
                    a.buildAnalysis(runner, analysis, manager.getParams().keySet());
                    analysis.setVisible(true);
                }
                if ((tabPane.getSelectionModel().getSelectedItem().getText().equals("Comparison") && !loaded) || force) {
                    comparison.setVisible(false);
                    c.buildComparison(runner, comparison, portfolio, manager.getParams());
                    comparison.setVisible(true);
                }
                if (!portfolio.isEmpty()) {
                    a.addRow(manager.getParams(), FormatUtils.round2dp(portfolio.getTotalReturnValue()));
                }
                if (force) {
                    loadedTabs.addAll(tabPane.getTabs());
                } else {
                    loadedTabs.add(selectedTab);
                }
                loader.setText("Loaded.");
                loader.setProgress(1.0);
            }
        };
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }

    private void addFileChoosers(HBox header) {
        HBox fileChoosers = new HBox();
        fileChoosers.setSpacing(50);

        //Choose csv file button
        AppFileChooser dataFileChooser = new AppFileChooser("Choose CSV", new ImageView(getClass().getResource("app-icons/choose-csv.png").toExternalForm()));
        addFileChooserListener(dataFileChooser);

        //Choose strategy module file button
        AppFileChooser strategyFileChooser = new AppFileChooser("Choose strategy", new ImageView(getClass().getResource("app-icons/choose-strat.png").toExternalForm()));
        addFileChooserListener(strategyFileChooser);

        //Choose parameters file button
        AppFileChooser paramFileChooser = new AppFileChooser("Choose parameters", new ImageView(getClass().getResource("app-icons/choose-settings.png").toExternalForm()));
        addFileChooserListener(paramFileChooser);

        fileChoosers.getChildren().addAll(dataFileChooser,strategyFileChooser,paramFileChooser);

        header.getChildren().add(fileChoosers);
    }

    private void addSettingsPanel(HBox header) {
        HBox settings = new HBox();
        settings.setAlignment(Pos.CENTER);
        settings.setSpacing(50);

        Button settingsButton = new Button("CHANGE\nPARAMETERS", new ImageView(getClass().getResource("app-icons/change-settings.png").toExternalForm()));
        settingsButton.getStyleClass().add("icon-button");
        settings.getChildren().add(settingsButton);
        initParamTable();
        settingsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.setTitle("Parameters");
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(stage);
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
                dialogVbox.getChildren().addAll(paramTable, close);
                Scene dialogScene = new Scene(dialogVbox);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        });

        //Run button
        Button runButton = new Button("",new ImageView(getClass().getResource("icons/run-circle.png").toExternalForm()));
        runButton.setId("run-button");
        settings.getChildren().add(runButton);

        header.getChildren().add(settings);

        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new Thread() {
                    public void run() {
                        if (runner.validFiles()) {
                            runButton.setDisable(true);
                            tabPane.setDisable(true);
                            updateParams();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    loader.setText("Running strategy...");
                                    loader.setProgress(0);
                                }
                            });
                            //new run means no tabs will have loaded
                            loadedTabs.clear();
                            runner.run(true);
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    loader.setText("Updating prices and orders information...");
                                    loader.setProgress(0.5);
                                }
                            });

                            priceReader = new PriceReader(runner.getDataFile());
                            priceReader.readAll();
                            //find the start and end date of the prices data
                            DateTime startDate = null, endDate = null;
                            for (String company: (Set<String>)priceReader.getHistory().getAllCompanies()) {
                                for (Price price: (List<Price>)priceReader.getCompanyHistory(company)) {
                                    if (startDate == null || price.getDate().isBefore(startDate)) {
                                        startDate = price.getDate();
                                    }
                                    if (endDate == null || price.getDate().isAfter(endDate)) {
                                        endDate = price.getDate();
                                    }
                                }
                            }
                            Set<String> priceCompaniesSet = priceReader.getHistory().getAllCompanies();
                            ObservableList<String> priceCompanies = FXCollections.observableArrayList(new ArrayList<>(priceCompaniesSet));

                            //update list of companies in the selector
                            if (companyListener != null) {
                                companySelector.valueProperty().removeListener(companyListener);
                            }
                            companySelector.getSelectionModel().clearSelection();
                            companySelector.setItems(priceCompanies);
                            companySelector.getSelectionModel().selectFirst();
                            addCompanySelectorListener();

                            List<Price> prices = priceReader.getCompanyHistory(priceCompanies.get(0));

                            if (FileUtils.matches(runner.getStrategyFile(),"aurora.jar")) {
                                orderReader = new OrderReaderKoK(OUTPUT_FILE_PATH);
                            } else {
                                orderReader = new OrderReader(OUTPUT_FILE_PATH);
                            }
                            orderReader.readAll();
                            List<Order> orders = orderReader.getCompanyHistory(priceCompanies.get(0));

                            portfolio = new Portfolio(orderReader.getHistory(), startDate, endDate);
                            loadContent(prices, orders, false);
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    loader.setText("");
                                    loader.setProgress(1);
                                }
                            });
                            runButton.setDisable(false);
                            tabPane.setDisable(false);
                        } else {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    AlertManager.info("Missing files", "Please check that you have selected all the required files.");
                                }
                            });
                        }
                    }
                }.start();
            }
        });
    }

    private void addFilterSelector() {
        HBox selector = new HBox();
        selector.setSpacing(20);
        selector.setPadding(new javafx.geometry.Insets(15, 15, 0, 15));
        addFilter("Company", selector);
        addDateFilters(selector);

        graph.setTop(selector);
    }

    private void addDateFilters(HBox selector) {
        HBox startDatePanel = new HBox();
        startDatePanel.getChildren().add(new Label("Start Date: "));
        selector.getChildren().add(startDatePanel);
        HBox endDatePanel = new HBox();
        endDatePanel.getChildren().add(new Label("End Date: "));
        selector.getChildren().add(endDatePanel);

        DatePicker startDatePicker = new DatePicker();
        configureDatePicker(startDatePicker);
        startDatePanel.getChildren().add(startDatePicker);
        startDatePicker.setOnAction(new EventHandler() {
            public void handle(javafx.event.Event t) {
                LocalDate date = startDatePicker.getValue();
                if (date == null) return;
                long startDate = date.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                g.updateLowerBound(startDate);
            }
        });
        DatePicker endDatePicker = new DatePicker();
        configureDatePicker(endDatePicker);
        endDatePanel.getChildren().add(endDatePicker);
        endDatePicker.setOnAction(new EventHandler() {
            public void handle(javafx.event.Event t) {
                LocalDate date = endDatePicker.getValue();
                if (date == null) return;
                long endDate = date.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                g.updateUpperBound(endDate);
            }
        });
        final Callback<DatePicker, DateCell> endDayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);

                                if (startDatePicker.getValue() != null && item.isBefore(startDatePicker.getValue())) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                            }
                        };
                    }
                };
        endDatePicker.setDayCellFactory(endDayCellFactory);
        final Callback<DatePicker, DateCell> startDayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);

                                if (endDatePicker.getValue() != null && item.isAfter(endDatePicker.getValue())) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                            }
                        };
                    }
                };
        startDatePicker.setDayCellFactory(startDayCellFactory);
    }

    private void configureDatePicker(DatePicker datePicker) {
        datePicker.setMinHeight(20);
        datePicker.setPromptText("dd/MM/yyyy");
        datePicker.setConverter(new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
    }

    private void addFilter(String name, HBox selector) {
        HBox filter = new HBox();
        companySelector = new ComboBox<>();
        new AutoCompleteComboBoxListener<>(companySelector);
        filter.getChildren().addAll(new Label(name + ": "), companySelector);
        selector.getChildren().add(filter);
    }

    private void initParamTable() {

        paramTable = new TableView();
        paramTable.setEditable(true);
        paramTable.setPlaceholder(new Label("No parameters available."));

        TableColumn keyCol = new TableColumn("Key");
        keyCol.setMinWidth(100);
        keyCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry, String> param) {
                String key = (String) param.getValue().getKey();
                return new SimpleStringProperty(key);
            }
        });

        TableColumn valueCol = new TableColumn("Value");
        valueCol.setEditable(true);
        valueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry, String> param) {
                String value = (String) param.getValue().getValue();
                return new SimpleStringProperty(value);
            }
        });
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());

        valueCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Map.Entry row = (Map.Entry)event.getRowValue();
                String newValue = (String)event.getNewValue();
                manager.put((String)row.getKey(), newValue);
            }
        });

        paramTable.getColumns().addAll(keyCol, valueCol);
        //ensures extra space to given to existing columns
        paramTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        paramTable.setMinWidth(300);
        //TODO Remove - only for testing
        updateParamTable();
    }

    private void updateParamTable() {
        if (runner.getParamFile() != null) {
            //remove previously stored parameters
            manager.clear();
            a.restart();

            Properties props = manager.getProperties(runner.getParamFile());
            Enumeration properties = props.propertyNames();
            ObservableList<Map.Entry> data = FXCollections.observableArrayList(props.entrySet());
            paramTable.setItems(data);

            while (properties.hasMoreElements()) {
                String key = (String) properties.nextElement();
                //if the value of the property is not numerical, it is not a parameter
                try {
                    Double.parseDouble(props.getProperty(key));
                } catch (NumberFormatException e) {
                    continue;
                }
                manager.put(key, props.getProperty(key));
            }
        }
    }

    private void updateParams() {
        if (manager.getNumParams() == 0) return;
        manager.updateParams(runner.getParamFile());
    }
}
