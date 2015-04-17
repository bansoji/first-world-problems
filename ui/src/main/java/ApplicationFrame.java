import humanize.Humanize;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.util.Callback;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

    private JFXPanel graph;
    private JFXPanel table;
    private JComboBox<String> companySelector;
    private JPanel selector;
    private JPanel settings;
    private JPanel paramSettings;
    private ParameterManager manager = new ParameterManager();

    private GraphBuilder g = new GraphBuilder();

    private static String APPLICATION_TITLE = "Trading Platform";
    private static String VERSION_NUMBER = "1.0";
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
        JPanel header = new AppPanel();
        header.setLayout(new BoxLayout(header,BoxLayout.Y_AXIS));

        JLabel title = new JLabel(APPLICATION_TITLE);
        title.setFont(title.getFont().deriveFont(28f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(50,0,5,0));
        header.add(title);

        JLabel appInfo = new JLabel(APPLICATION_INFO);
        appInfo.setFont(appInfo.getFont().deriveFont(10f));
        appInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        appInfo.setBorder(new EmptyBorder(0,0,30,0));
        header.add(appInfo);

        header.add(new JSeparator(SwingConstants.HORIZONTAL));
        add(header, BorderLayout.NORTH);
    }

    private void initBody()
    {
        final JPanel body = new AppPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        addFileChoosers(body);
        addSettingsPanel(body);
        addFilterSelector(body);

        JPanel content = new AppPanel();
        content.setLayout(new BorderLayout());
        graph = new JFXPanel();
        graph.setPreferredSize(getSize());
        content.add(graph, BorderLayout.CENTER);
        content.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);

        table = new JFXPanel();
        content.add(table, BorderLayout.EAST);
        body.add(content);

        add(body, BorderLayout.CENTER);
    }

    private void initFooter()
    {
        JLabel footer = new JLabel(FOOTER_MESSAGE);
        footer.setHorizontalAlignment(JLabel.CENTER);
        add(footer, BorderLayout.SOUTH);
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
        companySelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String companyName = (String) e.getItem();
                List<Price> prices = priceReader.getCompanyHistory(companyName);
                List<Order> orders = orderReader.getCompanyHistory(companyName);
                loadGraph(prices, orders);
                constructTable(prices, orders);
            }
        });
    }

    private void loadGraph(List<Price> prices, List<Order> orders)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                g.buildGraph(graph, prices, orders);
            }
        });
    }

    private void constructTable(List<Price> prices, List<Order> orders)
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
                TableBuilder.buildTable(table, prices, orderRecord);
            }
        });
    }

    private void addFileChoosers(JPanel body) {
        JPanel fileChoosers = new AppPanel();
        fileChoosers.setLayout(new FlowLayout(FlowLayout.LEADING));
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

        body.add(fileChoosers);
    }

    private void addSettingsPanel(JPanel body) {
        settings = new AppPanel();
        settings.setLayout(new FlowLayout(FlowLayout.LEADING));

        //Run button
        JButton runButton = new JButton("Run");
        settings.add(runButton);

        body.add(settings);

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
                        List<String> priceCompanies = new ArrayList<>(priceCompaniesSet);

                        //update list of companies in the selector
                        companySelector.removeAllItems();
                        for (String company: priceCompaniesSet) {
                            companySelector.addItem(company);
                        }

                        //update list of companies in the selector
                        companySelector.removeAllItems();
                        for (String company: priceCompaniesSet) {
                            companySelector.addItem(company);
                        }

                        List<Price> prices = priceReader.getCompanyHistory(priceCompanies.get(0));
                        selector.setVisible(true);

                        orderReader = new OrderReader(OUTPUT_FILE_PATH);
                        orderReader.readAll();
                        List<Order> orders = orderReader.getCompanyHistory(priceCompanies.get(0));
                        addCompanySelectorListener();
                        loadGraph(prices, orders);
                        constructTable(prices,orders);
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

    private void addFilterSelector(JPanel body) {
        selector = new AppPanel();
        selector.setBorder(new EmptyBorder(0,15,0,15));
        selector.setLayout(new FlowLayout(FlowLayout.LEADING));
        addFilter("Company");
        addDateFilters();

        selector.setVisible(false);
        body.add(new JSeparator(SwingConstants.HORIZONTAL));
        body.add(selector);
    }

    private void addDateFilters() {
        selector.add(new JLabel("Start Date: "));
        JFXPanel startDatePanel = new JFXPanel();
        selector.add(startDatePanel);
        selector.add(Box.createRigidArea(new Dimension(10,0)));
        selector.add(new JLabel("End Date: "));
        JFXPanel endDatePanel = new JFXPanel();
        selector.add(endDatePanel);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                DatePicker startDatePicker = new DatePicker();
                startDatePicker.setMinHeight(20);
                Scene startScene = new Scene(startDatePicker);
                startDatePanel.setScene(startScene);
                startDatePicker.setOnAction(new EventHandler() {
                    public void handle(javafx.event.Event t) {
                        LocalDate date = startDatePicker.getValue();
                        long startDate = date.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        g.updateLowerBound(startDate);
                    }
                });
                DatePicker endDatePicker = new DatePicker();
                endDatePicker.setMinHeight(20);
                Scene endScene = new Scene(endDatePicker);
                endDatePanel.setScene(endScene);
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
        selector.add(new JLabel(name + ": "));
        companySelector = new JComboBox<>();
        AutoCompleteDecorator.decorate(companySelector);
        selector.add(companySelector);
        selector.add(Box.createRigidArea(new Dimension(10,0)));
    }

    private void changeParamSelection() {
        Properties props = manager.getProperties(paramFile);
        if (paramSettings != null) settings.remove(paramSettings);
        paramSettings = new AppPanel();

        Enumeration properties = props.propertyNames();
        while (properties.hasMoreElements()) {
            JPanel panel = new AppPanel();
            String key = (String)properties.nextElement();
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
                        if (Integer.parseInt(value.getText()) > 0) {
                            String paramValue = value.getText();
                            manager.put(value.getName(), paramValue);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Error: Please enter number bigger than 0", "Error Massage",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null,
                                "Error: Please enter number bigger than 0", "Error Massage",
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
