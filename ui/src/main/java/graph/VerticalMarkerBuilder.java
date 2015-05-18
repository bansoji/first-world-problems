package graph;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

/**
 * Created by gavintam on 18/05/15.
 */
public class VerticalMarkerBuilder {
    public static void build(Pane parent, XYChart chart){
        addMouseListener(chart,new Marker(parent,chart));
    }
    private static void addMouseListener(XYChart chart, Marker marker) {
        EventHandler mouseHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED || mouseEvent.getEventType() == MouseEvent.MOUSE_MOVED) {
                    marker.update(mouseEvent.getX(), mouseEvent.getY());
                }
            }
        };
        chart.setOnMouseDragged(mouseHandler);
        chart.setOnMouseMoved(mouseHandler);
    }

    private static class Marker {
        Line marker;
        XYChart chart;
        private Marker(Pane parent, XYChart chart) {
            marker = new Line(0,0,0,0);
            marker.getStyleClass().add("vertical-marker");
            parent.getChildren().add(marker);
            this.chart = chart;
        }

        private void update(double x, double y) {
            Node chartArea = chart.lookup(".chart-plot-background");
            Bounds chartAreaBounds = chartArea.getBoundsInParent();
            marker.setStartX(Math.min(Math.max(x, chartAreaBounds.getMinX()), chartAreaBounds.getMaxX())+15);
            marker.setStartY(chartAreaBounds.getMinY()+15);
            marker.setEndX(Math.min(Math.max(x, chartAreaBounds.getMinX()), chartAreaBounds.getMaxX()) + 15);
            marker.setEndY(chartAreaBounds.getMaxY()+15);
        }
    }
}
