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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import main.*;
import org.joda.time.DateTime;

import javax.swing.*;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Gavin Tam on 17/03/15.
 */
public class ApplicationFrame extends Application {

    private static final Logger logger = Logger.getLogger("log");

    private StrategyRunner runner = new StrategyRunner();

    private Reader orderReader;
    private Reader priceReader;

    private Stage stage;
    private BorderPane main;
    private BorderPane graph;
    private GridPane stats;
    private BorderPane analysis;
    private ComboBox<String> companySelector;
    private ChangeListener companyListener;
    private HBox selector;
    private HBox settings;
    private Label loadingInfo;
    private ProgressBar loading;
    private ParameterManager<String> manager = new ParameterManager();
    private TableView paramTable;
    private TabPane tabPane;
    private HashSet<Tab> loadedTabs = new HashSet<>();

    private GraphBuilder g = new GraphBuilder();
    private AnalysisBuilder a = new AnalysisBuilder();
    private StatsBuilder s = new StatsBuilder();

    private static String VERSION_NUMBER = "1.1.0";
    private static String APPLICATION_INFO = "Version " + VERSION_NUMBER + "   \u00a9 Group 1";
    private static String FOOTER_MESSAGE = "Get the latest release at our website.";

    private static String OUTPUT_FILE_PATH = "orders.csv";

    @Override
    public void start(Stage primaryStage) throws Exception {
        //primaryStage.setFullScreen(true);
        primaryStage.setTitle("BuyHard Platform");
        main = new BorderPane();
        Scene scene = new Scene(main);
        scene.getStylesheets().addAll("general.css", "graph.css", "stats.css", "analysis.css");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(768);
        primaryStage.setMinWidth(1024);
        stage = primaryStage;
        initHeader();
        initBody();
        initFooter();
        primaryStage.show();
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
        addFilterSelector();

        tabPane = new TabPane();
        Tab tab = new Tab();
        tab.setText("Data");
        tab.setGraphic(new ImageView(getClass().getResource("app-icons/tab-data-icon.png").toExternalForm()));
        tab.setClosable(false);
        tab.setContent(graph);
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
        tabPane.getTabs().addAll(tab,statsTab,analysisTab);
        addTabLoadingAction(tabPane);
        //TODO Remove hack - for some reason the graph doesn't load for the first time
        loadContent(new History<>(), new ArrayList<>(), new ArrayList<>(), true);
        graph.setVisible(false);
        stats.setVisible(false);
        analysis.setVisible(false);

        body.getChildren().addAll(tabPane, new Separator());

        main.setCenter(body);
    }

    private void addTabLoadingAction(TabPane tabPane) {
        tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                if (!oldValue.equals(newValue)) {
                    if (orderReader == null || priceReader == null) return;
                    Set<String> companies = orderReader.getHistory().getAllCompanies();
                    String firstCompany = companies.iterator().next();
                    loadContent(orderReader.getHistory(), priceReader.getCompanyHistory(firstCompany), orderReader.getCompanyHistory(firstCompany), false);
                }
            }
        });
    }

    private void initFooter()
    {
        HBox footerPanel = new HBox();
        footerPanel.setAlignment(Pos.CENTER_LEFT);
        footerPanel.setId("footer");
        footerPanel.setSpacing(20);
        HBox footerText = new HBox();
        footerText.setId("footer-text");
        HBox.setHgrow(footerText,Priority.ALWAYS);
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
        loading = new ProgressBar(0);
        loadingInfo = new Label();
        footerPanel.getChildren().addAll(loading, loadingInfo,footerText);
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
        if (companyListener == null) {
            companyListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String old, String companyName) {
                    List<Price> prices = priceReader.getCompanyHistory(companyName);
                    List<Order> orders = orderReader.getCompanyHistory(companyName);
                    loadedTabs.remove(tabPane.getSelectionModel().getSelectedItem());
                    loadContent(orderReader.getHistory(), prices, orders, false);
                }
            };
        }
        companySelector.valueProperty().addListener(companyListener);
    }

    private void loadContent(History<Order> history, List<Price> prices, List<Order> orders, boolean force)
    {
        Map<DateTime, OrderType> orderRecord = new HashMap<>();
        if (orders != null) {
            for (Order order : orders) {
                orderRecord.put(order.getOrderDate(), order.getOrderType());
            }
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                boolean loaded = loadedTabs.contains(selectedTab);
                if (loaded && !force) {
                    return;
                }
                if ((tabPane.getSelectionModel().getSelectedItem().getText().equals("Data") && !loaded) || force) {
                    graph.setVisible(false);
                    g.buildGraph(graph, prices, orders, orderRecord);
                    graph.setVisible(true);
                }
                if ((tabPane.getSelectionModel().getSelectedItem().getText().equals("Portfolio") && !loaded) || force) {
                    stats.setVisible(false);
                    s.build(stats, history, prices, orderRecord);
                    stats.setVisible(true);
                }
                if ((tabPane.getSelectionModel().getSelectedItem().getText().equals("Analysis") && !loaded) || force) {
                    analysis.setVisible(false);
                    a.buildAnalysis(runner, analysis, manager.getParams().keySet());
                    analysis.setVisible(true);
                }
                if (force) {
                    loadedTabs.addAll(tabPane.getTabs());
                } else {
                    loadedTabs.add(selectedTab);
                }
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
        settings = new HBox();
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
        Button runButton = new Button("",new ImageView(getClass().getResource("icons/run.png").toExternalForm()));
        runButton.setId("run-button");
        settings.getChildren().addAll(runButton);

        header.getChildren().add(settings);

        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new Thread() {
                    public void run() {
                        if (runner.validFiles()) {
                            tabPane.setDisable(true);
                            updateParams();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    loadingInfo.setText("Running strategy...");
                                    loading.setProgress(0);
                                }
                            });
                            //new run means no tabs will have loaded
                            loadedTabs.clear();
                            runner.run(true);
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    loadingInfo.setText("Updating prices and orders information...");
                                    loading.setProgress(0.5);
                                }
                            });

                            priceReader = new PriceReader(runner.getDataFile());
                            priceReader.readAll();
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
                            selector.setVisible(true);

                            orderReader = new OrderReader(OUTPUT_FILE_PATH);
                            orderReader.readAll();
                            List<Order> orders = orderReader.getCompanyHistory(priceCompanies.get(0));

                            a.addRow(manager.getParams(), FormatUtils.round2dp(new Portfolio(orderReader.getHistory()).getTotalReturnValue()));
                            loadContent(orderReader.getHistory(), prices, orders, false);
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    loadingInfo.setText("");
                                    loading.setProgress(1);
                                }
                            });
                            tabPane.setDisable(false);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Please check that you have selected all the required files.",
                                    "Missing files", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }.start();
            }
        });
    }

    private void addFilterSelector() {
        selector = new HBox();
        selector.setSpacing(20);
        selector.setPadding(new javafx.geometry.Insets(15, 15, 0, 15));
        addFilter("Company");
        addDateFilters();

        selector.setVisible(false);
        graph.setTop(selector);
    }

    private void addDateFilters() {
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

    private void addFilter(String name) {
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
        updateParamTable();
    }

    private void updateParamTable() {
        //remove previously stored parameters
        manager.clear();
        a.restart();

        Properties props = manager.getProperties(runner.getParamFile());
        Enumeration properties = props.propertyNames();
        ObservableList<Map.Entry> data = FXCollections.observableArrayList(props.entrySet());
        paramTable.setItems(data);

        while (properties.hasMoreElements()) {
            String key = (String)properties.nextElement();
            //if the value of the property is not numerical, it is not a parameter
            try {
                Double.parseDouble(props.getProperty(key));
            } catch (NumberFormatException e) {
                continue;
            }
            manager.put(key,props.getProperty(key));
        }
    }

    private void updateParams() {
        if (manager.getNumParams() == 0) return;
        manager.updateParams(runner.getParamFile());
    }
}
