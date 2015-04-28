import humanize.Humanize;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class ApplicationFrame extends JFrame {

    private static final Logger logger = Logger.getLogger("log");

    //TODO Remove values - only for TESTING
    private String strategyFile = "out/artifacts/trading_jar/trading.jar";
    private String dataFile = "common/src/main/resources/sampleDataSmall";
    private String paramFile = "trading/resources/config.properties";

    private Reader orderReader;
    private Reader priceReader;

    private BorderPane graph;
    private GridPane stats;
    private JFXPanel content;
    private ComboBox<String> companySelector;
    private ChangeListener companyListener;
    private HBox selector;
    private JPanel settings;
    private JPanel paramSettings;
    private ParameterManager manager = new ParameterManager();

    private GraphBuilder g = new GraphBuilder();

    private static String VERSION_NUMBER = "1.0.0";
    private static String APPLICATION_INFO = "Version " + VERSION_NUMBER + "   \u00a9 Group 1";
    private static String FOOTER_MESSAGE = "Get the latest release at our website.";

    private static String OUTPUT_FILE_PATH = "orders.csv";

    public ApplicationFrame()
    {
        super();
        getContentPane().setBackground(Color.WHITE);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setMinimumSize(new Dimension (1024,768));
        setLayout(new BorderLayout());
        initHeader();
        initBody();
        initFooter();
        setLocationRelativeTo(null);    //centre frame
    }

    private void initHeader()
    {
        JFXPanel header = new JFXPanel();
        header.setLayout(new BoxLayout(header,BoxLayout.X_AXIS));
        header.setBorder(new EmptyBorder(10,0,10,0));

//        HBox pane = new HBox();
//        pane.setSpacing(10);
//        Image image = new Image("ui/src/main/resources/logosizes/BuyHardLogo_Small.png");
//
//        // simple displays ImageView the image as is
//        ImageView logo = new ImageView();
//        logo.setImage(image);
//        pane.getChildren().addAll(logo, new Label(APPLICATION_INFO));
//        Scene scene = new Scene(pane);
//        header.setScene(scene);


        JLabel title = new JLabel();
        title.setIcon(new ImageIcon(getClass().getResource("logosizes/BuyHardLogo_Small.png")));
        header.add(title);

        header.add(Box.createRigidArea(new Dimension(20,0)));

        JLabel appInfo = new JLabel(APPLICATION_INFO);
        appInfo.setFont(appInfo.getFont().deriveFont(10f));
        header.add(appInfo);

        addFileChoosers(header);
        addSettingsPanel(header);

        add(header, BorderLayout.NORTH);
    }

    private void initBody()
    {
        final JPanel body = new AppPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        content = new JFXPanel();
        content.setLayout(new BorderLayout());

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

        Scene scene = new Scene(tabPane);
        scene.getStylesheets().addAll("general.css", "graph.css", "stats.css");
        content.setScene(scene);

        body.add(content);
        body.add(new JSeparator(SwingConstants.HORIZONTAL));

        add(body, BorderLayout.CENTER);
    }

    private void initFooter()
    {
        JPanel footerPanel = new AppPanel();
        JLabel footer = new JLabel(FOOTER_MESSAGE);
        footer.setHorizontalAlignment(JLabel.CENTER);
        footerPanel.add(footer);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void addFileChooserListener(final FileChooser fileChooser)
    {
        fileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userDirLocation = System.getProperty("user.dir");
                File userDir = new File(userDirLocation);
                JFileChooser fc = new JFileChooser(userDir);
                int val = fc.showOpenDialog(getContentPane());
                if (val == JFileChooser.APPROVE_OPTION)
                {
                    try {
                        if (fileChooser.getButtonText().equals("Choose CSV")) {
                            dataFile = fc.getSelectedFile().getAbsolutePath();
                        } else if (fileChooser.getButtonText().equals("Choose strategy")) {
                            strategyFile = fc.getSelectedFile().getAbsolutePath();
                        } else if (fileChooser.getButtonText().equals("Choose parameters")) {
                            paramFile = fc.getSelectedFile().getAbsolutePath();
                            changeParamSelection();
                        } else {
                            return;
                        }
                        fileChooser.setLabelText(fc.getSelectedFile().getName());
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Map<Date,OrderType> orderRecord = new HashMap<>();
                if (orders != null) {
                    for (Order order : orders) {
                        orderRecord.put(order.getOrderDate(), order.getOrderType());
                    }
                }
                g.buildGraph(graph,prices, orders, orderRecord);
                StatsBuilder.build(stats,history, prices, orderRecord);
            }
        });
    }

    private void addFileChoosers(JFXPanel header) {
        JPanel fileChoosers = new JPanel();
        fileChoosers.setOpaque(false);
        fileChoosers.setLayout(new FlowLayout(FlowLayout.TRAILING));
        //fileChoosers.setBorder(new EmptyBorder(20,0,0,0));

        //Choose csv file button
        FileChooser dataFileChooser = new FileChooser("Choose CSV");
        addFileChooserListener(dataFileChooser);
        fileChoosers.add(dataFileChooser);

        //Choose strategy module file button
        FileChooser strategyFileChooser = new FileChooser("Choose strategy");
        addFileChooserListener(strategyFileChooser);
        fileChoosers.add(strategyFileChooser);

        //Choose parameters file button
        FileChooser paramFileChooser = new FileChooser("Choose parameters");
        addFileChooserListener(paramFileChooser);
        fileChoosers.add(paramFileChooser);

        header.add(fileChoosers);
    }

    private void addSettingsPanel(JFXPanel header) {
        settings = new JPanel();
        settings.setOpaque(false);
        //settings.setBorder(new EmptyBorder(0, 5, 10, 0));
        settings.setLayout(new FlowLayout(FlowLayout.LEADING));

        //Run button
        JButton runButton = new JButton("Run");
        settings.add(runButton);

        header.add(settings);

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        });
    }

    private void addFilterSelector() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                selector = new HBox();
                selector.setSpacing(20);
                selector.setPadding(new javafx.geometry.Insets(15, 15, 0, 15));
                addFilter("Company");
                addDateFilters();

                selector.setVisible(false);
                graph.setTop(selector);
            }
        });
    }

    private void addDateFilters() {
        HBox startDatePanel = new HBox();
        startDatePanel.getChildren().add(new Label("Start Date: "));
        selector.getChildren().add(startDatePanel);
        HBox endDatePanel = new HBox();
        endDatePanel.getChildren().add(new Label("End Date: "));
        selector.getChildren().add(endDatePanel);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
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
        });
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
        if (paramSettings != null) settings.remove(paramSettings);
        paramSettings = new AppPanel();
        Enumeration properties = props.propertyNames();
        while (properties.hasMoreElements()) {
            String key = (String)properties.nextElement();
            //if the value of the property is not numerical, it is not a parameter
            try {
                Double.parseDouble(props.getProperty(key));
            } catch (NumberFormatException e) {
                continue;
            }
            JPanel panel = new AppPanel();
            JLabel label = new JLabel(Humanize.capitalize(Humanize.decamelize(key)) + ": ");
            panel.add(label);
            JTextField value = new JTextField(props.getProperty(key));
            value.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    warn();
                }

                public void removeUpdate(DocumentEvent e) {
                    warn();
                }

                public void insertUpdate(DocumentEvent e) {
                    warn();
                }

                public void warn() {
                    if (value.getText().equals("")) return;
                    try {
                        if (Double.parseDouble(value.getText()) >= 0) {
                            String paramValue = value.getText();
                            manager.put(value.getName(), paramValue);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Error: Please enter a non-negative number", "Error Massage",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null,
                                "Error: Please enter a non-negative number", "Error Massage",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            value.setName(key);
            panel.add(value);
            paramSettings.add(panel);
        }
        settings.add(paramSettings);
    }

    private void updateParams() {
        if (manager.getNumParams() == 0) return;
        manager.updateParams(paramFile);
    }
}
