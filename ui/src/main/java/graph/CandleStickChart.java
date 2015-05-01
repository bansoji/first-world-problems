/*
 * Copyright (c) 2008, 2011 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//Sourced from https://code.google.com/p/javafx-ui-hxzon/source/browse/trunk/ChartsSampler/chartssampler/?r=13
package graph;

import java.util.*;

import color.ColorManager;
import date.DateUtils;
import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import org.joda.time.DateTime;

/**
 * A candlestick chart is a style of bar-chart used primarily to describe price movements of a security, derivative,
 * or currency over time.
 *
 * The Data Y value is used for the opening price and then the close, high and low values are stored in the Data's
 * extra value property using a CandleStickExtraValues object.
 */
public class CandleStickChart extends XYChart<Long,Number> {

    private HashSet<Node> candlesShown = new HashSet<>();

    // -------------- CONSTRUCTORS ----------------------------------------------

    /**
     * Construct a new CandleStickChart with the given axis.
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     */
    public CandleStickChart(Axis<Long> xAxis, Axis<Number> yAxis) {
        super(xAxis,yAxis);
        setAnimated(false);
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);
    }

    /**
     * Construct a new CandleStickChart with the given axis and data.
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
     */
    public CandleStickChart(Axis<Long> xAxis, Axis<Number> yAxis, ObservableList<Series<Long,Number>> data) {
        this(xAxis, yAxis);
        setData(data);
    }

    // -------------- METHODS ------------------------------------------------------------------------------------------

    /** Called to update and layout the content for the plot */
    @Override protected void layoutPlotChildren() {
        // we have nothing to layout if no data is present
        if(getData() == null) return;
        // update candle positions
        for (int seriesIndex=0; seriesIndex < getData().size(); seriesIndex++) {
            Series<Long,Number> series = getData().get(seriesIndex);
            Iterator<Data<Long,Number>> iter = getDisplayedDataIterator(series);
            Path seriesPath = null;
            if (series.getNode() instanceof Path) {
                seriesPath = (Path)series.getNode();
                seriesPath.getElements().clear();
            }

            int j = 0;
            boolean removeAllCandles = false;
            while(iter.hasNext()) {
                Data<Long,Number> item = iter.next();
                double x = getXAxis().getDisplayPosition(getCurrentDisplayedXValue(item));
                double y = getYAxis().getDisplayPosition(getCurrentDisplayedYValue(item));
                CandleStickExtraValues extra = (CandleStickExtraValues)item.getExtraValue();

                if ((series.getNode() instanceof Path) && extra != null) {
                    if (((ValueAxis)getXAxis()).getUpperBound()-((ValueAxis)getXAxis()).getLowerBound() < 15e9) {
                        Candle candle = (Candle)createCandle(seriesIndex,item,j++);
                        candle.setOpacity(1);
                        if (shouldAnimate()) {
                            getPlotChildren().add(candle);
                            candlesShown.add(candle);
                            // fade in new candle
                            FadeTransition ft = new FadeTransition(Duration.millis(500),candle);
                            ft.setToValue(1);
                            ft.play();
                        } else if (!candlesShown.contains(candle)) {
                            getPlotChildren().add(candle);
                            candlesShown.add(candle);
                        }

                        double close = getYAxis().getDisplayPosition(extra.getClose());
                        double high = getYAxis().getDisplayPosition(extra.getHigh());
                        double low = getYAxis().getDisplayPosition(extra.getLow());
                        // calculate candle width
                        double candleWidth = -1;
                        if (getXAxis() instanceof DateValueAxis) {
                            //DateValueAxis xa = (DateValueAxis) getXAxis();
                            //candleWidth = xa.getDisplayPosition((long)xa.getTickUnit()) * 0.90; // use 90% width between ticks
                            candleWidth = 10;
                        }
                        // update candle
                        candle.update(close - y, high - y, low - y, candleWidth);
                        candle.updateTooltip(((CandleStickExtraValues) item.getExtraValue()).getType(), item.getXValue(), item.getYValue().doubleValue(), extra.getClose(), extra.getHigh(), extra.getLow());

                        // position the candle
                        candle.setLayoutX(x);
                        candle.setLayoutY(y);
                    } else {
                        removeAllCandles = true;
                    }
                }
                if (seriesPath != null) {
                    if (seriesPath.getElements().isEmpty()) {
                        seriesPath.getElements().add(new MoveTo(x,getYAxis().getDisplayPosition(extra.getAverage())));
                    } else {
                        seriesPath.getElements().add(new LineTo(x,getYAxis().getDisplayPosition(extra.getAverage())));
                    }
                }
            }
            if (removeAllCandles) {
                for (Object n: getPlotChildren().toArray()) {
                    if (n instanceof Candle) {
                        getPlotChildren().remove(n);
                        candlesShown.remove(n);
                    }
                }
            }
        }
    }

    @Override protected void dataItemChanged(Data<Long, Number> item) {}

    @Override protected void dataItemAdded(Series<Long,Number> series, int itemIndex, Data<Long,Number> item) {
        Node candle = createCandle(getData().indexOf(series), item, itemIndex);
        if (shouldAnimate()) {
            candle.setOpacity(0);
            getPlotChildren().add(candle);
            // fade in new candle
            FadeTransition ft = new FadeTransition(Duration.millis(500), candle);
            ft.setToValue(1);
            ft.play();
        } else {
            getPlotChildren().add(candle);
        }
        // always draw average line on top
        if (series.getNode() != null) series.getNode().toFront();
    }

    @Override protected  void dataItemRemoved(Data<Long,Number> item, Series<Long,Number> series) {
        final Node candle = item.getNode();
        if (shouldAnimate()) {
            // fade out old candle
            FadeTransition ft = new FadeTransition(Duration.millis(500), candle);
            ft.setToValue(0);
            ft.setOnFinished(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent actionEvent) {
                    getPlotChildren().remove(candle);
                }
            });
            ft.play();
        } else {
            getPlotChildren().remove(candle);
        }
    }

    @Override protected  void seriesAdded(Series<Long,Number> series, int seriesIndex) {
        //don't add candles yet as for large data sets, it's really slow
        // create series path
        Path seriesPath = new Path();
        seriesPath.getStyleClass().setAll("candlestick-average-line","series"+seriesIndex);
        series.setNode(seriesPath);
        getPlotChildren().add(seriesPath);
    }

    @Override protected  void seriesRemoved(Series<Long,Number> series) {
        // remove all candle nodes
        for (XYChart.Data<Long,Number> d : series.getData()) {
            final Node candle = d.getNode();
            if (shouldAnimate()) {
                // fade out old candle
                FadeTransition ft = new FadeTransition(Duration.millis(500),candle);
                ft.setToValue(0);
                ft.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent actionEvent) {
                        getPlotChildren().remove(candle);
                    }
                });
                ft.play();
            } else {
                getPlotChildren().remove(candle);
            }
        }
    }

    /**
     * Create a new Candle node to represent a single data item
     *
     * @param seriesIndex The index of the series the data item is in
     * @param item        The data item to create node for
     * @param itemIndex   The index of the data item in the series
     * @return New candle node to represent the give data item
     */
    private Node createCandle(int seriesIndex, final Data item, int itemIndex) {
        Node candle = item.getNode();
        // check if candle has already been created
        if (candle instanceof Candle) {
            ((Candle) candle).setSeriesAndDataStyleClasses("series" + seriesIndex, "data" + itemIndex);
        } else {
            candle = new Candle("series" + seriesIndex, "data" + itemIndex);
            item.setNode(candle);
        }
        return candle;
    }

    /**
     * This is called when the range has been invalidated and we need to update it. If the axis are auto
     * ranging then we compile a list of all data that the given axis has to plot and call invalidateRange() on the
     * axis passing it that data.
     */
    @Override protected void updateAxisRange() {
        // For candle stick chart we need to override this method as we need to let the axis know that they need to be able
        // to cover the whole area occupied by the high to low range not just its center data value
        final DateValueAxis xa = (DateValueAxis)getXAxis();
        final Axis<Number> ya = getYAxis();
        List<Long> xData = null;
        List<Number> yData = null;
        if(xa.isAutoRanging()) xData = new ArrayList<Long>();
        if(ya.isAutoRanging()) yData = new ArrayList<Number>();
        if(xData != null || yData != null) {
            for(Series<Long,Number> series : getData()) {
                for(Data<Long,Number> data: series.getData()) {
                    if(xData != null) {
                        xData.add(data.getXValue());
                    }
                    if(yData != null){
                        CandleStickExtraValues extras = (CandleStickExtraValues)data.getExtraValue();
                        if (extras !=null) {
                            yData.add(extras.getHigh());
                            yData.add(extras.getLow());
                        } else {
                            yData.add(data.getYValue());
                        }
                    }
                }
            }
            if(xData != null) xa.invalidateRange(xData);
            if(yData != null) ya.invalidateRange(yData);
        }
    }

    /** Data extra values for storing close, high and low. */
    public static class CandleStickExtraValues {
        private double close;
        private double high;
        private double low;
        private double average;
        private NodeType type;

        public CandleStickExtraValues(NodeType type, double close, double high, double low, double average) {
            this.close = close;
            this.high = high;
            this.low = low;
            this.average = average;
            this.type = type;
        }

        public double getClose() {
            return close;
        }

        public double getHigh() {
            return high;
        }

        public double getLow() {
            return low;
        }

        public double getAverage() {
            return average;
        }

        public NodeType getType() {
            return type;
        }
    }

    /** Candle node used for drawing a candle */
    private static class Candle extends Group {
        private Line highLowLine = new Line();
        private Region bar = new Region();
        private String seriesStyleClass;
        private String dataStyleClass;
        private boolean openAboveClose = true;
        private Tooltip tooltip = new Tooltip();

        private Candle(String seriesStyleClass, String dataStyleClass) {
            setAutoSizeChildren(false);
            getChildren().addAll(highLowLine, bar);
            this.seriesStyleClass = seriesStyleClass;
            this.dataStyleClass = dataStyleClass;
            updateStyleClasses();
            tooltip.setGraphic(new TooltipContent());
            Tooltip.install(bar,tooltip);
        }

        public void setSeriesAndDataStyleClasses(String seriesStyleClass , String dataStyleClass) {
            this.seriesStyleClass = seriesStyleClass;
            this.dataStyleClass = dataStyleClass;
            updateStyleClasses();
        }

        public void update(double closeOffset, double highOffset, double lowOffset, double candleWidth) {
            openAboveClose = closeOffset > 0;
            updateStyleClasses();
            highLowLine.setStartY(highOffset);
            highLowLine.setEndY(lowOffset);
            if (candleWidth == -1) candleWidth = bar.prefWidth(-1);
            if(openAboveClose) {
                bar.resizeRelocate(-candleWidth/2,0,candleWidth,closeOffset);
            } else {
                bar.resizeRelocate(-candleWidth/2,closeOffset,candleWidth,closeOffset*-1);
            }
        }

        public void updateTooltip(NodeType type, long date, double open, double close, double high, double low) {
            TooltipContent tooltipContent = (TooltipContent)tooltip.getGraphic();
            tooltipContent.update(type, date, open, close, high, low);
        }

        private void updateStyleClasses() {
            getStyleClass().setAll("candlestick-candle",seriesStyleClass,dataStyleClass);
            highLowLine.getStyleClass().setAll("candlestick-line",seriesStyleClass,dataStyleClass,
                    openAboveClose?"open-above-close":"close-above-open");
            bar.getStyleClass().setAll("candlestick-bar",seriesStyleClass,dataStyleClass,
                    openAboveClose?"open-above-close":"close-above-open");
        }
    }

    private static class TooltipContent extends GridPane {
        private Label dateValue = new Label();
        private Label openValue = new Label();
        private Label closeValue = new Label();
        private Label highValue = new Label();
        private Label lowValue = new Label();

        private TooltipContent() {
            Label open = new Label("OPEN:");
            Label close = new Label("CLOSE:");
            Label high = new Label("HIGH:");
            Label low = new Label("LOW:");
            getStyleClass().add("tooltip-content");

            setConstraints(dateValue, 0, 0);
            setConstraints(open,0,1);
            setConstraints(openValue,1,1);
            setConstraints(close,0,2);
            setConstraints(closeValue,1,2);
            setConstraints(high,0,3);
            setConstraints(highValue,1,3);
            setConstraints(low,0,4);
            setConstraints(lowValue, 1, 4);
            getChildren().addAll(dateValue, open, openValue, close, closeValue, high, highValue, low, lowValue);
        }

        public void update(NodeType type, long date, double open, double close, double high, double low) {
            dateValue.setText(DateUtils.formatMonthAbbr(new DateTime(date)));
            openValue.setText(Double.toString(open));
            closeValue.setText(Double.toString(close));
            highValue.setText(Double.toString(high));
            lowValue.setText(Double.toString(low));

            switch (type) {
                case BuyOrder:
                    closeValue.setStyle("-fx-text-fill: " + ColorManager.BUY);
                    break;
                case SellOrder:
                    closeValue.setStyle("-fx-text-fill: " + ColorManager.SELL);
                    break;
            }
        }
    }
}