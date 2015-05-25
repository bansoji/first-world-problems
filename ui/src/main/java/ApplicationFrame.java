import alert.AlertManager;
import components.AppFileChooser;
import dialog.DialogBuilder;
import file.StrategyRunner;
import format.FormatChecker;
import format.FormatUtils;
import image.ImageUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.*;
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
import javafx.stage.Stage;
import javafx.util.Callback;
import core.*;
import org.joda.time.DateTime;
import report.ReportGenerator;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
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
    private static final String WEBSITE_LINK = "http://cgi.cse.unsw.edu.au/~awondo/se3011/web/";

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

    private Loader loader;

    private ParameterManager<String> manager = new ParameterManager();
    private TableView paramTable;
    private HashSet<Tab> loadedTabs = new HashSet<>();

    private Portfolio portfolio = new Portfolio(new History<>(),null,null);

    private GraphBuilder g = new GraphBuilder();
    private AnalysisBuilder a = new AnalysisBuilder();
    private StatsBuilder s = new StatsBuilder();
    private ComparisonBuilder c = new ComparisonBuilder();

    private static final String VERSION_NUMBER = "1.2.0";
    private static final String APPLICATION_INFO = "Version " + VERSION_NUMBER + "   \u00a9 Group 1";
    private static final String FOOTER_MESSAGE = "Get the latest release at our website.";

    @Override
    public void start(Stage primaryStage) throws Exception {
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
        ImageView logo = ImageUtils.getImage("logosizes/BuyHard2Logo_Small.png");
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

        tabPane = new TabPane();
        Tab dataTab = constructTab("Data", ImageUtils.getImage("app-icons/tab-data-icon.png"), graph);
        addHelpModal(dataTab,ImageUtils.getImage("images/mouse-graph.jpeg"));

        Tab statsTab = constructTab("Portfolio", ImageUtils.getImage("app-icons/tab-portfolio-icon.png"), stats);
        Tab analysisTab = constructTab("Analysis", ImageUtils.getImage("app-icons/tab-analysis-icon.png"), analysis);
        Tab comparisonTab = constructTab("Comparison", ImageUtils.getImage("app-icons/tab-comparison-icon.png"), comparison);

        tabPane.getTabs().addAll(dataTab, statsTab, analysisTab, comparisonTab);
        addTabLoadingAction(tabPane);

        body.getChildren().addAll(tabPane, new Separator());

        main.setCenter(body);
    }

    private Tab constructTab(String name, ImageView icon, Node content) {
        Tab tab = new Tab(name,content);
        tab.setGraphic(icon);
        tab.setClosable(false);
        return tab;
    }

    private void addHelpModal(Tab tab, Node node) {
        final MenuItem helpItem = new MenuItem("Help", ImageUtils.getImage("icons/help.png"));
        helpItem.setOnAction(DialogBuilder.constructHelpModal(node));
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
        HBox.setHgrow(footerText,Priority.ALWAYS);
        footerText.setAlignment(Pos.CENTER_RIGHT);
        Label appInfo = new Label(APPLICATION_INFO);
        Hyperlink websiteLink = new Hyperlink(FOOTER_MESSAGE);
        websiteLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                getHostServices().showDocument(WEBSITE_LINK);
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
                    g.buildGraph(graph, prices, orders, priceReader, orderReader, orderRecord);
                    graph.setVisible(true);
                }
                if ((tabPane.getSelectionModel().getSelectedItem().getText().equals("Portfolio") && !loaded) || force) {
                    stats.setVisible(false);
                    s.build(stats, portfolio);
                    stats.setVisible(true);
                }
                if ((tabPane.getSelectionModel().getSelectedItem().getText().equals("Analysis") && !loaded) || force) {
                    analysis.setVisible(false);
                    a.buildAnalysis(runner, analysis, manager.getParams().keySet());
                    analysis.setVisible(true);
                }
                if ((tabPane.getSelectionModel().getSelectedItem().getText().equals("Comparison") && !loaded) || force) {
                    comparison.setVisible(false);
                    c.buildComparison(runner, comparison, portfolio, prices, priceReader, manager.getParams());
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
        AppFileChooser dataFileChooser = new AppFileChooser("Choose CSV", ImageUtils.getImage("app-icons/choose-csv.png"));
        addFileChooserListener(dataFileChooser);

        //Choose strategy module file button
        AppFileChooser strategyFileChooser = new AppFileChooser("Choose strategy", ImageUtils.getImage("app-icons/choose-strat.png"));
        addFileChooserListener(strategyFileChooser);

        //Choose parameters file button
        AppFileChooser paramFileChooser = new AppFileChooser("Choose parameters", ImageUtils.getImage("app-icons/choose-settings.png"));
        addFileChooserListener(paramFileChooser);

        fileChoosers.getChildren().addAll(dataFileChooser,strategyFileChooser,paramFileChooser);

        header.getChildren().add(fileChoosers);
    }

    private void addSettingsPanel(HBox header) {
        HBox settings = new HBox();
        settings.setAlignment(Pos.CENTER);
        settings.setSpacing(50);

        Button settingsButton = new Button("CHANGE\nPARAMETERS", ImageUtils.getImage("app-icons/change-settings.png"));
        settingsButton.getStyleClass().add("icon-button");
        settings.getChildren().add(settingsButton);
        initParamTable();
        settingsButton.setOnAction(DialogBuilder.constructSimpleDialog("Parameters", paramTable));

        //Run button
        Button runButton = new Button("",ImageUtils.getImage("icons/run-circle.png"));
        runButton.getStyleClass().add("header-button");
        settings.getChildren().add(runButton);

        MenuButton exportButton = new MenuButton("",ImageUtils.getImage("icons/export-icon.png"));
        exportButton.getStyleClass().add("header-button");
        MenuItem exportToPdf = new MenuItem("Export to PDF", ImageUtils.getImage("icons/pdf.png"));
        MenuItem exportToExcel = new MenuItem("Export to Excel", ImageUtils.getImage("icons/excel.png"));
        MenuItem screenshot = new MenuItem("Screenshot", ImageUtils.getImage("icons/screenshot.png"));

        exportButton.getItems().addAll(exportToPdf, exportToExcel, screenshot);
        exportToExcel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new Thread() {
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                FileChooser fileChooser = new FileChooser();
                                fileChooser.setTitle("Export Excel file");
                                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel Document", ".xlsx"));
                                File file = fileChooser.showSaveDialog(stage);
                                loader.setProgress(0);
                                loader.setText("Generating Excel file...");
                                if (file != null) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            ReportGenerator g = new ReportGenerator(portfolio);
                                            g.generateXLS(file.getAbsolutePath());
                                        }
                                    }.start();
                                }
                                loader.setProgress(1);
                                loader.setText("Loaded.");
                            }
                        });
                    }
                }.start();
            }
        });
        exportToPdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loader.setProgress(0);
                        loader.setText("Generating preview for PDF file...");
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Export PDF file");
                        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF file", ".pdf"));
                        File file = fileChooser.showSaveDialog(stage);
                        loader.setProgress(0);
                        loader.setText("Generating Excel file...");
                        if (file != null) {
                            new Thread() {
                                @Override
                                public void run() {
                                    ReportGenerator g = new ReportGenerator(portfolio);
                                    g.generatePDF(file.getAbsolutePath());
                                }
                            }.start();
                        }
                        loader.setProgress(1);
                        loader.setText("Loaded.");
                    }
                });
            }
        });
        screenshot.setOnAction(new EventHandler<ActionEvent>() {
               public void handle(ActionEvent e) {
                   FileChooser fileChooser = new FileChooser();
                   fileChooser.setTitle("Save Image");
                   File file = fileChooser.showSaveDialog(stage);
                   if (file != null) {
                       try {
                           ImageIO.write(SwingFXUtils.fromFXImage(stage.getScene().snapshot(null),
                                   null), "png", file);
                       } catch (IOException ex) {
                           System.out.println(ex.getMessage());
                       }
                   }
               }
           }
        );
        settings.getChildren().add(exportButton);
        exportButton.setDisable(true);  //disable until run button is pressed at least once

        header.getChildren().add(settings);

        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new Thread() {
                    public void run() {
                        if (runner.validFiles()) {
                            runButton.setDisable(true);
                            exportButton.setDisable(true);
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

                            List<Price> prices = priceReader.getCompanyHistory(priceCompanies.get(0));

                            //integration with other JARs
                            orderReader = IntegrationUtils.selectReader(runner.getStrategyFile());
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
                            exportButton.setDisable(false);
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
                //if the value of the property is not numerical, it is not a parameter we need to record
                if (FormatChecker.isDouble(props.getProperty(key))) {
                    manager.put(key, props.getProperty(key));
                }
            }
        }
    }

    private void updateParams() {
        if (manager.getNumParams() == 0) return;
        manager.updateParams(runner.getParamFile());
    }
}
