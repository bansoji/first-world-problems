import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gavin Tam on 17/03/15.
 */
public class ApplicationFrame extends JFrame {

    //TODO Remove values - only for TESTING
    private String strategyFile = "out/artifacts/trading_jar/trading.jar";
    private String dataFile = "common/src/main/resources/sampleDataSmall";
    private String paramFile = "trading/resources/config.properties";

    private JFXPanel graph;
    private JFXPanel table;

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
        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setLayout(new BoxLayout(header,BoxLayout.Y_AXIS));

//        try {
//            GraphicsEnvironment ge =
//                    GraphicsEnvironment.getLocalGraphicsEnvironment();
//            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("ui/src/main/resources/fonts.Open_Sans/OpenSans-Light.ttf")));
//        } catch (IOException|FontFormatException e) {
//            //Handle exception
//            System.out.println(e.getMessage());
//        }
        JLabel title = new JLabel(APPLICATION_TITLE);
//        Font font = new Font("Open Sans Light", Font.PLAIN, 28);
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
        final JPanel body = new JPanel();
        body.setBackground(Color.WHITE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel fileChoosers = new JPanel();
        fileChoosers.setBackground(Color.WHITE);
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

        //Run button
        JButton runButton = new JButton("Run");
        body.add(runButton);
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (strategyFile != null && dataFile != null && paramFile != null) {
                    try {
                        ProcessBuilder pb = new ProcessBuilder("java", "-jar", strategyFile, dataFile, paramFile);
                        pb.start();
                        Reader reader = new PriceReader(dataFile);
                        reader.readAll();
                        List<Price> prices = reader.getCompanyHistory("BHP.AX");
                        reader = new OrderReader(OUTPUT_FILE_PATH);
                        reader.readAll();
                        List<Order> orders = reader.getCompanyHistory("BHP.AX");
                        loadGraph(prices, orders);
                        Map<Date,OrderType> orderRecord = new HashMap<>();
                        for (Order order: orders)
                        {
                            orderRecord.put(order.getOrderDate(),order.getOrderType());
                        }
                        constructTable(prices,orderRecord);
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

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
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

    private void loadGraph(List<Price> prices, List<Order> orders)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GraphBuilder.buildGraph(graph, prices, orders);
            }
        });
    }

    private void constructTable(List<Price> prices, Map<Date,OrderType> orders)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                TableBuilder.buildTable(table, prices, orders);
            }
        });
    }
}
