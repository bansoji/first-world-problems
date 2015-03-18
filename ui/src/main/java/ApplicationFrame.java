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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Gavin Tam on 17/03/15.
 */
public class ApplicationFrame extends JFrame {

    private String strategyFile;
    private String dataFile;

    private TransactionReader reader;

    private JFXPanel graph;

    private static String APPLICATION_TITLE = "Trading Platform by Group 1";
    private static String FOOTER_MESSAGE = "Get the latest release at our website.";

    public ApplicationFrame()
    {
        super();
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
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

        //Choose csv file button
        JButton dataFileChooser = new JButton("Choose CSV file");
        addFileChooserListener(dataFileChooser);
        body.add(dataFileChooser);

        //Choose strategy module file button
        JButton strategyFileChooser = new JButton("Choose strategy file");
        addFileChooserListener(strategyFileChooser);
        body.add(dataFileChooser);

        //Run button
        JButton runButton = new JButton("Run");
        body.add(runButton);
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try
                {
                    Runtime rt = Runtime.getRuntime();
                    rt.exec("java -jar " + strategyFile + " " + dataFile);
                }
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog (null,
                            "An unexpected error has occurred when running the given files.",
                            "Files could not be run", JOptionPane.ERROR_MESSAGE);
                }
                loadGraph();
            }
        });
        body.add(new JSeparator(SwingConstants.HORIZONTAL));
        graph = new JFXPanel();
        body.add(graph);
        add(body, BorderLayout.CENTER);
    }

    private void initFooter()
    {
        JLabel footer = new JLabel(FOOTER_MESSAGE);
        footer.setHorizontalAlignment(JLabel.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void addFileChooserListener(final JButton fileChooser)
    {
        fileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int val = fc.showOpenDialog(getContentPane());
                if (val == JFileChooser.APPROVE_OPTION)
                {
                    try {
                        if (fileChooser.getText().equals("Choose CSV file")) {
                            dataFile = fc.getSelectedFile().getAbsolutePath();
                        } else if (fileChooser.getText().equals("Choose strategy file")) {
                            strategyFile = fc.getSelectedFile().getAbsolutePath();
                        }
                    }
                    catch (Exception ex)
                    {
                        System.err.println(ex.getMessage());
                    }
                }
            }
        });
    }

    private void loadGraph()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //TODO
                buildGraph(null);
            }
        });
    }

    private void buildGraph(ArrayList<Price> prices)
    {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(
                xAxis, yAxis);

        if (prices.size() > 0) {
            lineChart.setTitle("Price of " + prices.get(0).getCompanyName());
            XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
            // populating the series with data

            for (int i = 0; i < prices.size(); i++) {
                XYChart.Data data = new XYChart.Data<Date, Number>(prices.get(i).getDate(), prices.get(i).getValue());
                series.getData().add(data);
                data.setNode(new HoverPane(i == 0 ? 0 : prices.get(i-1).getValue(), prices.get(i-1).getValue()));
            }
            lineChart.getData().add(series);
        }
        graph.setScene(new Scene(lineChart));
    }
}
