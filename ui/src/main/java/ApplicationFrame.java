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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.*;
import org.joda.time.DateTime;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Gavin Tam on 17/03/15.
 */
public class ApplicationFrame extends Application {

    private static final Logger logger = Logger.getLogger("log");

    //TODO Remove values - only for TESTING
    private String strategyFile = "out/artifacts/trading_jar/trading.jar";
    private String dataFile = "common/src/main/resources/sampleDataSmall";
    private String paramFile = "trading/resources/config.properties";

    private Reader orderReader;
    private Reader priceReader;

    private Stage stage;
    private BorderPane main;
    private BorderPane graph;
    private GridPane stats;
    private ComboBox<String> companySelector;
    private ChangeListener companyListener;
    private HBox selector;
    private HBox settings;
    private ParameterManager manager = new ParameterManager();
    private TableView paramTable;

    private GraphBuilder g = new GraphBuilder();

    private static String VERSION_NUMBER = "1.0.0";
    private static String APPLICATION_INFO = "Version " + VERSION_NUMBER + "   \u00a9 Group 1";
    private static String FOOTER_MESSAGE = "Get the latest release at our website.";

    private static String OUTPUT_FILE_PATH = "orders.csv";

    @Override
    public void start(Stage primaryStage) throws Exception {
        //primaryStage.setFullScreen(true);
        primaryStage.setTitle("BuyHard Platform");
        main = new BorderPane();
        Scene scene = new Scene(main);
        scene.getStylesheets().addAll("general.css", "graph.css", "stats.css");
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
        ImageView logo = new ImageView(getClass().getResource("logosizes/BuyHardLogo_Small.png").toExternalForm());
        Label info = new Label(APPLICATION_INFO);
        appInfo.setAlignment(Pos.CENTER_LEFT);
        appInfo.getChildren().addAll(logo, info);
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
        addFilterSelector();

        TabPane tabPane = new TabPane();
        Tab tab = new Tab();
        tab.setText("Data");
        tab.setClosable(false);
        tab.setContent(graph);
        Tab statsTab = new Tab();
        statsTab.setText("Portfolio");
        statsTab.setClosable(false);
        statsTab.setContent(stats);
        tabPane.getTabs().addAll(tab,statsTab);
        //TODO Remove hack - for some reason the graph doesn't load for the first time
        loadContent(new History<>(), new ArrayList<>(),new ArrayList<>());
        graph.setVisible(false);

        body.getChildren().addAll(tabPane);

        main.setCenter(body);
    }

    private void initFooter()
    {
        HBox footerPanel = new HBox();
        footerPanel.setId("footer");
        Label footer = new Label(FOOTER_MESSAGE);
        footerPanel.getChildren().add(footer);
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
                        if (fileChooser.getButtonText().equals("Choose CSV")) {
                            dataFile = file.getAbsolutePath();
                        } else if (fileChooser.getButtonText().equals("Choose strategy")) {
                            strategyFile = file.getAbsolutePath();
                        } else if (fileChooser.getButtonText().equals("Choose parameters")) {
                            paramFile = file.getAbsolutePath();
                            changeParamSelection();
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
                    loadContent(orderReader.getHistory(), prices, orders);
                }
            };
        }
        companySelector.valueProperty().addListener(companyListener);
    }

    private void loadContent(History<Order> history, List<Price> prices, List<Order> orders)
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
                g.buildGraph(graph, prices, orders, orderRecord);
                StatsBuilder.build(stats, history, prices, orderRecord);
                graph.setVisible(true);
            }
        };
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }

    private void addFileChoosers(HBox header) {
        HBox fileChoosers = new HBox();
        fileChoosers.setSpacing(50);

        //Choose csv file button
        AppFileChooser dataFileChooser = new AppFileChooser("Choose CSV");
        addFileChooserListener(dataFileChooser);

        //Choose strategy module file button
        AppFileChooser strategyFileChooser = new AppFileChooser("Choose strategy");
        addFileChooserListener(strategyFileChooser);

        //Choose parameters file button
        AppFileChooser paramFileChooser = new AppFileChooser("Choose parameters");
        addFileChooserListener(paramFileChooser);

        fileChoosers.getChildren().addAll(dataFileChooser,strategyFileChooser,paramFileChooser);

        header.getChildren().add(fileChoosers);
    }

    private void addSettingsPanel(HBox header) {
        settings = new HBox();
        settings.setAlignment(Pos.CENTER);
        settings.setSpacing(50);

        Button settingsButton = new Button("Change parameters");
        settings.getChildren().add(settingsButton);
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
                changeParamSelection();
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
        Button runButton = new Button("Run");
        settings.getChildren().addAll(runButton);

        header.getChildren().add(settings);

        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new Thread() {
                    public void run() {
                        if (strategyFile != null && dataFile != null && paramFile != null) {
                            try {
                                updateParams();
                                ProcessBuilder pb = new ProcessBuilder("java", "-jar", strategyFile, dataFile, paramFile);
                                Process p = pb.start();
                                try {
                                    p.waitFor();
                                } catch (InterruptedException ex) {
                                    logger.severe("Failed to run module without errors.");
                                    return;
                                }

                                priceReader = new PriceReader(dataFile);
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
                                loadContent(orderReader.getHistory(), prices, orders);
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(null,
                                        "An unexpected error has occurred when running the given files." +
                                                "Please check that the file content is in the correct format",
                                        "Files could not be run", JOptionPane.ERROR_MESSAGE);
                            }
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
        startDatePicker.setMinHeight(20);
        startDatePanel.getChildren().add(startDatePicker);
        startDatePicker.setOnAction(new EventHandler() {
            public void handle(javafx.event.Event t) {
                LocalDate date = startDatePicker.getValue();
                long startDate = date.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                g.updateLowerBound(startDate);
            }
        });
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setMinHeight(20);
        endDatePanel.getChildren().add(endDatePicker);
        endDatePicker.setOnAction(new EventHandler() {
            public void handle(javafx.event.Event t) {
                LocalDate date = endDatePicker.getValue();
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

    private void addFilter(String name) {
        HBox filter = new HBox();
        companySelector = new ComboBox<>();
        new AutoCompleteComboBoxListener<>(companySelector);
        filter.getChildren().addAll(new Label(name + ": "), companySelector);
        selector.getChildren().add(filter);
    }

    private void changeParamSelection() {
        //remove previously stored parameters
        manager.clear();
        Properties props = manager.getProperties(paramFile);
        Enumeration properties = props.propertyNames();
        while (properties.hasMoreElements()) {
            String key = (String)properties.nextElement();
            //if the value of the property is not numerical, it is not a parameter
            try {
                Double.parseDouble(props.getProperty(key));
            } catch (NumberFormatException e) {
                continue;
            }
            paramTable = new TableView();
            paramTable.setEditable(true);
            ObservableList<Map.Entry> data = FXCollections.observableArrayList(props.entrySet());
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
            paramTable.setItems(data);
        }
    }

    private void updateParams() {
        if (manager.getNumParams() == 0) return;
        manager.updateParams(paramFile);
    }
}
