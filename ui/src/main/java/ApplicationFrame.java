import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Gavin Tam on 17/03/15.
 */
public class ApplicationFrame extends JFrame {

    //TODO Remove values - only for TESTING
    private String strategyFile = "out/artifacts/trading_jar/trading.jar";
    private String dataFile = "common/src/main/resources/sampleDataSmall";
    private String paramFile = "";

    private JFXPanel graph;

    private static String APPLICATION_TITLE = "Trading Platform by Group 1";
    private static String FOOTER_MESSAGE = "Get the latest release at our website.";

    private static String OUTPUT_FILE_PATH = "output.csv";

    public ApplicationFrame()
    {
        super();
        URL theURL = this.getClass().getResource("graph.css");
        System.out.println(theURL.toString());
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
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header,BoxLayout.Y_AXIS));

        JLabel title = new JLabel(APPLICATION_TITLE);
        title.setFont(header.getFont().deriveFont(28f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(50,0,50,0));
        header.add(title);

        header.add(new JSeparator(SwingConstants.HORIZONTAL));
        add(header, BorderLayout.NORTH);
    }

    private void initBody()
    {
        final JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel fileChoosers = new JPanel();
        fileChoosers.setLayout(new FlowLayout(FlowLayout.LEADING));
        //Choose csv file button
        FileChooser dataFileChooser = new FileChooser("Choose CSV file");
        addFileChooserListener(dataFileChooser);
        fileChoosers.add(dataFileChooser);

        //Choose strategy module file button
        FileChooser strategyFileChooser = new FileChooser("Choose strategy file");
        addFileChooserListener(strategyFileChooser);
        fileChoosers.add(strategyFileChooser);

        //Choose parameters file button
        FileChooser paramFileChooser = new FileChooser("Choose parameters file");
        addFileChooserListener(paramFileChooser);
        fileChoosers.add(paramFileChooser);

        body.add(fileChoosers);

        //Run button
        JButton runButton = new JButton("Run");
        body.add(runButton);
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (strategyFile != null && dataFile != null && paramFile != null) {
                    try {
                        Runtime rt = Runtime.getRuntime();
                        rt.exec("java -jar " + strategyFile + " " + dataFile + " " + paramFile);
                        TransactionReader reader = new TransactionReader(dataFile);
                        List<Price> prices = reader.getAllPrices();
                        reader = new TransactionReader(OUTPUT_FILE_PATH);
                        List<Order> orders = reader.getAllOrders();
                        loadGraph(prices,orders);
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
        body.add(new JSeparator(SwingConstants.HORIZONTAL));
        graph = new JFXPanel();
        graph.setPreferredSize(getSize());
        body.add(graph);
        body.add(new JSeparator(SwingConstants.HORIZONTAL));
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
                        if (fileChooser.getButtonText().equals("Choose CSV file")) {
                            dataFile = fc.getSelectedFile().getAbsolutePath();
                        } else if (fileChooser.getButtonText().equals("Choose strategy file")) {
                            strategyFile = fc.getSelectedFile().getAbsolutePath();
                        } else if (fileChooser.getButtonText().equals("Choose parameters file")) {
                            paramFile = fc.getSelectedFile().getAbsolutePath();
                        } else {
                            return;
                        }
                        fileChooser.setLabelText(fc.getSelectedFile().getName());
                    }
                    catch (Exception ex)
                    {
                        System.err.println(ex.getMessage());
                    }
                }
            }
        });
    }

    private void loadGraph(final List<Price> prices, final List<Order> orders)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                buildGraph(prices,orders);
            }
        });
    }

    private void buildGraph(List<Price> prices, List<Order> orders)
    {
        final DateAxis xAxis = new DateAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Price");
        final LineChart<Date, Number> lineChart = new LineChart<>(
                xAxis, yAxis);

        if (prices.size() > 0) {
            lineChart.setTitle("Price of " + prices.get(0).getCompanyName());
            XYChart.Series<Date, Number> series = new XYChart.Series<>();
            // populating the series with data

            Iterator<Order> orderIterator = orders.iterator();
            Order currOrder = orderIterator.hasNext() ? orderIterator.next() : null;
            for (int i = 0; i < prices.size(); i++) {
                XYChart.Data data = new XYChart.Data<Date, Number>(prices.get(i).getDate(), prices.get(i).getValue());
                series.getData().add(data);
                ORDER_SEARCH:
                {
                    while (true) {
                        //if an order is placed at this price
                        if (currOrder.getOrderDate().equals(prices.get(i).getDate())) {
                            if (currOrder.getOrderType().equals(OrderType.BUY)) {
                                data.setNode(new InfoBox(currOrder.getPrice(), currOrder.getOrderDate(), InfoBox.InfoBoxType.BuyOrder));
                            } else {
                                data.setNode(new InfoBox(currOrder.getPrice(), currOrder.getOrderDate(), InfoBox.InfoBoxType.SellOrder));
                            }
                            //if no order is placed at this price
                        } else if (currOrder.getOrderDate().after(prices.get(i).getDate())) {
                            data.setNode(new InfoBox(prices.get(i).getValue(), prices.get(i).getDate(), InfoBox.InfoBoxType.Price));
                        } else if (orderIterator.hasNext()) {
                            currOrder = orderIterator.next();
                            continue;
                        }
                        break ORDER_SEARCH;
                    }
                }
            }
            lineChart.getData().add(series);
            lineChart.setLegendVisible(false);
            yAxis.setForceZeroInRange(false);
        }
        Scene scene = new Scene(lineChart);
        //NOTE: Remember to add .css to the compiler settings in Intellij
        scene.getStylesheets().add("graph.css");
        graph.setScene(scene);
    }
}
