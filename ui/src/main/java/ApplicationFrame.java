import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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

    private static String APPLICATION_TITLE = "Trading Platform";
    private static String VERSION_NUMBER = "1.0";
    private static String APPLICATION_INFO = "Version " + VERSION_NUMBER + "   \u00a9 Group 1";
    private static String FOOTER_MESSAGE = "Get the latest release at our website.";

    private static String OUTPUT_FILE_PATH = "output.csv";

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
                        Runtime rt = Runtime.getRuntime();
                        rt.exec("java -jar " + strategyFile + " " + dataFile + " " + paramFile);
                        TransactionReader reader = new TransactionReader(dataFile);
                        List<Price> prices = reader.getAllPrices();
                        reader = new TransactionReader(OUTPUT_FILE_PATH);
                        List<Order> orders = reader.getAllOrders();
                        loadGraph(prices, orders);
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
                GraphBuilder.buildGraph(graph,prices,orders);
            }
        });
    }
}
