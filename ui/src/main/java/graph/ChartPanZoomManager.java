package graph;

import javafx.event.EventHandler;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;

/**
 * Created by gavintam on 8/05/15.
 */
public class ChartPanZoomManager {
    public static Region setup(XYChart chart) {
        ChartPanManager panManager = new ChartPanManager(chart);
        //panning using right mouse button
        panManager.setMouseFilter(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!event.getButton().equals(MouseButton.SECONDARY))
                    event.consume();
            }
        });
        panManager.start();
        return JFXChartUtil.setupZooming(chart);
    }

    public static void addResetZoomFunction(XYChart chart) {
        //reset zoom if left-clicked twice
        chart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    chart.getXAxis().setAutoRanging(true);
                    chart.getYAxis().setAutoRanging(true);
                }
            }
        });
    }
}
