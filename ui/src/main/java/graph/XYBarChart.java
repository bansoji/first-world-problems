package graph;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.css.converters.SizeConverter;
import date.DateUtils;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Pedro
 * Date: 29-08-2013
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class XYBarChart extends XYChart<Long, Number> {
    // -------------- PRIVATE FIELDS -------------------------------------------

    private Map<Series, Map<Object, Data<Long, Number>>> seriesCategoryMap = new HashMap<Series, Map<Object, Data<Long, Number>>>();
    private TreeSet categories = new TreeSet();

    private Legend legend = new Legend();
    private boolean seriesRemove = false;
    private final Orientation orientation;

    private ValueAxis valueAxis;

    private Timeline dataRemoveTimeline;
    private Data<Long, Number> dataItemBeingRemoved = null;
    private Series<Long, Number> seriesOfDataRemoved = null;
    private double bottomPos = 0;
    private static String NEGATIVE_STYLE = "negative";
    // -------------- PUBLIC PROPERTIES ----------------------------------------

    /**
     * The gap to leave between bars in the same category
     */
    private DoubleProperty barGap = new StyleableDoubleProperty(2) {
        @Override
        protected void invalidated() {
            get();
            requestChartLayout();
        }

        public Object getBean() {
            return XYBarChart.this;
        }

        public String getName() {
            return "barGap";
        }

        public CssMetaData<XYBarChart, Number> getCssMetaData() {
            return StyleableProperties.BAR_GAP;
        }
    };

    public final double getBarGap() {
        return barGap.getValue();
    }

    public final void setBarGap(double value) {
        barGap.setValue(value);
    }

    public final DoubleProperty barGapProperty() {
        return barGap;
    }

    /**
     * The gap to leave between bars in separate categories
     */
    private DoubleProperty categoryGap = new StyleableDoubleProperty(10) {
        @Override
        protected void invalidated() {
            get();
            requestChartLayout();
        }

        @Override
        public Object getBean() {
            return XYBarChart.this;
        }

        @Override
        public String getName() {
            return "categoryGap";
        }

        public CssMetaData<XYBarChart, Number> getCssMetaData() {
            return StyleableProperties.CATEGORY_GAP;
        }
    };

    public final double getCategoryGap() {
        return categoryGap.getValue();
    }

    public final void setCategoryGap(double value) {
        categoryGap.setValue(value);
    }

    public final DoubleProperty categoryGapProperty() {
        return categoryGap;
    }

    // -------------- CONSTRUCTOR ----------------------------------------------

    /**
     * Construct a new graph.plugins.XYBarChart with the given axis.
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     */
    public XYBarChart(Axis<Long> xAxis, Axis<Number> yAxis) {
        this(xAxis, yAxis, FXCollections.<Series<Long, Number>>observableArrayList());

    }

    /**
     * Construct a new graph.plugins.XYBarChart with the given axis and data.
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     * @param data  The data to use, this is the actual list used so any changes to it will be reflected in the chart
     */
    public XYBarChart(Axis<Long> xAxis, Axis<Number> yAxis, ObservableList<Series<Long, Number>> data) {
        super(xAxis, yAxis);
        getStyleClass().add("bar-chart");
        setLegend(legend);

//        if (xAxis instanceof CategoryAxis) {
//            categoryAxis = (CategoryAxis)xAxis;
//            valueAxis = (ValueAxis)yAxis;
//            orientation = Orientation.VERTICAL;
//        } else {
//            categoryAxis = (CategoryAxis)yAxis;
//            valueAxis = (ValueAxis)xAxis;
//            orientation = Orientation.HORIZONTAL;
//        }
        orientation = Orientation.VERTICAL;

        // assuming value axis is the second axis
        valueAxis = (ValueAxis) yAxis;
        // update css
        pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, orientation == Orientation.HORIZONTAL);
        pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, orientation == Orientation.VERTICAL);
        setData(data);
    }

    /**
     * Construct a new graph.plugins.XYBarChart with the given axis and data.
     *
     * @param xAxis       The x axis to use
     * @param yAxis       The y axis to use
     * @param data        The data to use, this is the actual list used so any changes to it will be reflected in the chart
     * @param categoryGap The gap to leave between bars in separate categories
     */
    public XYBarChart(Axis<Long> xAxis, Axis<Number> yAxis, ObservableList<Series<Long, Number>> data, double categoryGap) {
        this(xAxis, yAxis);
        setData(data);
        setCategoryGap(categoryGap);
    }

    // -------------- PROTECTED METHODS ----------------------------------------

    @Override
    protected void dataItemAdded(Series<Long, Number> series, int itemIndex, Data<Long, Number> item) {
        Object category;
        if (orientation == Orientation.VERTICAL) {
            category = item.getXValue();
        } else {
            category = item.getYValue();
        }

        categories.add(category);

        Map<Object, Data<Long, Number>> categoryMap = seriesCategoryMap.get(series);

        if (categoryMap == null) {
            categoryMap = new HashMap<Object, Data<Long, Number>>();
            seriesCategoryMap.put(series, categoryMap);
        }
//        // check if category is already present
//        if (!categoryAxis.getCategories().contains(category)) {
//            // note: cat axis categories can be updated only when autoranging is true.
//            categoryAxis.getCategories().add(itemIndex, category);
//        } else if (categoryMap.containsKey(category)){
        if (categoryMap.containsKey(category)) {
            // RT-21162 : replacing the previous data, first remove the node from scenegraph.
            Data data = categoryMap.get(category);
            getPlotChildren().remove(data.getNode());
            removeDataItemFromDisplay(series, data);
            requestChartLayout();
            categoryMap.remove(category);
        }
        categoryMap.put(category, item);
        Node bar = createBar(series, getData().indexOf(series), item, itemIndex);
        if (shouldAnimate()) {
            if (dataRemoveTimeline != null && dataRemoveTimeline.getStatus().equals(Animation.Status.RUNNING)) {
                if (dataItemBeingRemoved != null && dataItemBeingRemoved == item) {
                    dataRemoveTimeline.stop();
                    getPlotChildren().remove(bar);
                    removeDataItemFromDisplay(seriesOfDataRemoved, item);
                    dataItemBeingRemoved = null;
                    seriesOfDataRemoved = null;
                }
            }
            animateDataAdd(item, bar);

        } else {
            getPlotChildren().add(bar);
        }
        getPlotChildren().add(bar);
    }

    @Override
    protected void dataItemRemoved(final Data<Long, Number> item, final Series<Long, Number> series) {
        final Node bar = item.getNode();
        if (shouldAnimate()) {
            dataRemoveTimeline = createDataRemoveTimeline(item, bar, series);
            dataItemBeingRemoved = item;
            seriesOfDataRemoved = series;
            dataRemoveTimeline.setOnFinished(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    ReflectionUtils.forceMethodCall(XYChart.Data.class, "setSeries", item, new Object[]{null}); // TODO: make sure this is working as expected
                    getPlotChildren().remove(bar);
                    removeDataItemFromDisplay(series, item);
                    dataItemBeingRemoved = null;
                    updateMap(series, item);
                }
            });
            dataRemoveTimeline.play();
        } else {
            ReflectionUtils.forceMethodCall(XYChart.Data.class, "setSeries", item, new Object[]{null}); // TODO: make sure this is working as expected
            getPlotChildren().remove(bar);
            removeDataItemFromDisplay(series, item);
            updateMap(series, item);
        }

    }

    /**
     * @inheritDoc
     */
    @Override
    protected void dataItemChanged(Data<Long, Number> item) {
        double barVal;
        double currentVal;
        if (orientation == Orientation.VERTICAL) {
            barVal = ((Number) item.getYValue()).doubleValue();
            //currentVal = ((Number)item.getCurrentY()).doubleValue();
            Number currentY = (Number) ReflectionUtils.forceMethodCall(Data.class, "getCurrentY", item);
            currentVal = currentY.doubleValue();
        } else {
            barVal = ((Number) item.getXValue()).doubleValue();
            //currentVal = ((Number)item.getCurrentX()).doubleValue();
            Number currentX = (Number) ReflectionUtils.forceMethodCall(Data.class, "getCurrentX", item);
            currentVal = currentX.doubleValue();
        }
        if (currentVal > 0 && barVal < 0) { // going from positive to negative
            // add style class negative
            item.getNode().getStyleClass().add(NEGATIVE_STYLE);
        } else if (currentVal < 0 && barVal > 0) { // going from negative to positive
            // remove style class negative
            // RT-21164 upside down bars: was adding NEGATIVE_STYLE styleclass
            // instead of removing it; when going from negative to positive
            item.getNode().getStyleClass().remove(NEGATIVE_STYLE);
        }
    }

    @Override
    protected void seriesAdded(Series<Long, Number> series, int seriesIndex) {
        // handle any data already in series
        // create entry in the map
        Map<Object, Data<Long, Number>> categoryMap = new HashMap<Object, Data<Long, Number>>();
        for (int j = 0; j < series.getData().size(); j++) {
            Data<Long, Number> item = series.getData().get(j);
            Node bar = createBar(series, seriesIndex, item, j);
            Object category;
            if (orientation == Orientation.VERTICAL) {
                category = item.getXValue();
            } else {
                category = item.getYValue();
            }
            categoryMap.put(category, item);

            categories.add(category);

            if (shouldAnimate()) {
                animateDataAdd(item, bar);
            } else {
                // RT-21164 check if bar value is negative to add NEGATIVE_STYLE style class
                double barVal = (orientation == Orientation.VERTICAL) ? ((Number) item.getYValue()).doubleValue() :
                        ((Number) item.getXValue()).doubleValue();
                if (barVal < 0) {
                    bar.getStyleClass().add(NEGATIVE_STYLE);
                }
                getPlotChildren().add(bar);
            }

//            RT-21164 check if bar value is negative to add NEGATIVE_STYLE style class
//            double barVal = (orientation == Orientation.VERTICAL) ? ((Number)item.getYValue()).doubleValue() :
//                    ((Number)item.getXValue()).doubleValue();
//            if (barVal < 0) {
//                bar.getStyleClass().add(NEGATIVE_STYLE);
//            }
//            getPlotChildren().add(bar);
        }
        if (categoryMap.size() > 0) seriesCategoryMap.put(series, categoryMap);
    }

    @Override
    protected void seriesRemoved(final Series<Long, Number> series) {
        updateDefaultColorIndex(series);
        // remove all symbol nodes
        if (shouldAnimate()) {
            ParallelTransition pt = new ParallelTransition();
            pt.setOnFinished(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    removeSeriesFromDisplay(series);
                }
            });
            for (final Data<Long, Number> d : series.getData()) {
                final Node bar = d.getNode();
                seriesRemove = true;
                // Animate series deletion
                if (/*getSeriesSize()*/((int) ReflectionUtils.forceMethodCall(XYChart.class, "getSeriesSize", this)) > 1) {
                    for (int j = 0; j < series.getData().size(); j++) {
                        Data<Long, Number> item = series.getData().get(j);
                        Timeline t = createDataRemoveTimeline(item, bar, series);
                        pt.getChildren().add(t);
                    }
                } else {
                    // fade out last series
                    FadeTransition ft = new FadeTransition(Duration.millis(700), bar);
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            getPlotChildren().remove(bar);
                            updateMap(series, d);
                        }
                    });
                    pt.getChildren().add(ft);
                }
            }
            pt.play();
        } else {
            for (Data<Long, Number> d : series.getData()) {
                final Node bar = d.getNode();
                getPlotChildren().remove(bar);
                updateMap(series, d);
            }
            removeSeriesFromDisplay(series);
        }
    }

    static int layoutPlotChildrenCount = 1;

    /**
     * @inheritDoc
     */
    @Override
    protected void layoutPlotChildren() {
        //System.out.println("Laying out plot children number - " + layoutPlotChildrenCount);

        double barWidth = 0;
        double categorySize = 0;
        SortedSet categoriesOnScreen = getCategoriesOnScreen();
        if (categoriesOnScreen == null)
            return;

        categorySize = calculateCategorySize(categoriesOnScreen);
        barWidth = calculateBarWidth(categorySize);

        if (categorySize ==  0) categorySize = getData().size() / 2d;
        if (barWidth == 0) barWidth = 0.5;

//        do
//        {
//            categorySize = calculateCategorySize(categoriesOnScreen);
//            barWidth = calculateBarWidth(categorySize);
//            if(barWidth <= 1)
//            {
//                categoriesOnScreen.remove(categoriesOnScreen.first());
//                if (categoriesOnScreen.size() > 1)
//                    categoriesOnScreen.remove(categoriesOnScreen.last());
//
//                if (categoriesOnScreen.size() > 0)
//                    zoomTo(categoriesOnScreen);
//            }
//        } while (barWidth <= 1 && categoriesOnScreen != null && categoriesOnScreen.size() > 0);

        final double barOffset = -(categorySize / 2d);
        final double zeroPos = (valueAxis.getLowerBound() > 0) ?
                valueAxis.getDisplayPosition(valueAxis.getLowerBound()) : valueAxis.getZeroPosition();
        // update bar positions and sizes
        for (Object category : categories) {
            int index = 0;

            ObservableList<Series<Long, Number>> allSeries = (ObservableList<Series<Long, Number>>) ReflectionUtils.forceMethodCall(XYChart.class, "getData", this);
            for (int i = 0; i < allSeries.size(); i++) {
                Series<Long, Number> series = allSeries.get(i);
                final Data<Long, Number> item = getDataItem(series, category);
                if (item != null) {
                    final Node bar = item.getNode();
                    double categoryPos = -1;
                    double valPos = -1;

                    if (categoriesOnScreen != null && categoriesOnScreen.contains(category))
                    {
                        if (orientation == Orientation.VERTICAL) {
//                            categoryPos = getXAxis().getDisplayPosition((X) graph.plugins.ReflectionUtils.forceMethodCall(Data.class, "getCurrentX", item));
                            categoryPos = getXAxis().getDisplayPosition(item.getXValue());

                            valPos = getYAxis().getDisplayPosition((Number) ReflectionUtils.forceMethodCall(Data.class, "getCurrentY", item));
                        }
                        else {
                            categoryPos = getYAxis().getDisplayPosition((Number) ReflectionUtils.forceMethodCall(Data.class, "getCurrentY", item));
                            valPos = getXAxis().getDisplayPosition((Long) ReflectionUtils.forceMethodCall(Data.class, "getCurrentX", item));
                        }
                        final double bottom = Math.min(valPos, zeroPos);
                        final double top = Math.max(valPos, zeroPos);
                        bottomPos = bottom;
                        if (orientation == Orientation.VERTICAL) {
                            bar.resizeRelocate(categoryPos + barOffset + (barWidth + getBarGap()) * index,
                                    bottom, barWidth, top - bottom);

                            // visual debugging purposes
//                            Line lineSegment = new Line(categoryPos, bottom, categoryPos, top);
//                            getPlotChildren().add(lineSegment);
                        } else {
                            //noinspection SuspiciousNameCombination
                            bar.resizeRelocate(bottom, categoryPos + barOffset + (barWidth + getBarGap()) * index,
                                    top - bottom, barWidth);
                        }
                        bar.setVisible(true);
                        index++;
                    }
                    else
                    {
                        bar.setVisible(false);
                    }
                }
            }
        }
    }

    private double calculateCategorySize(SortedSet categories) {
        double halfCategorySize = Double.POSITIVE_INFINITY;
        ValueAxis xAxis = (ValueAxis) getXAxis();

        double firstMark = xAxis.getDisplayPosition(xAxis.getLowerBound());
        Iterator iterator = categories.iterator();
        double secondMark = xAxis.getDisplayPosition((long)iterator.next());
        halfCategorySize = Math.min(secondMark - firstMark, halfCategorySize);
        firstMark = secondMark;
        for (; iterator.hasNext();)
        {
            secondMark = xAxis.getDisplayPosition((long) iterator.next());
            halfCategorySize = Math.min((secondMark - firstMark) / 2d, halfCategorySize);
            firstMark = secondMark;
        }
        halfCategorySize = Math.min(xAxis.getDisplayPosition(xAxis.getUpperBound()) - xAxis.getDisplayPosition((long) categories.last()), halfCategorySize);

        return halfCategorySize * 2;
    }

    private double calculateBarWidth(double categorySize)
    {
        int nSeries = (int) ReflectionUtils.forceMethodCall(XYChart.class, "getSeriesSize", this);
        double barWidth = (categorySize - (nSeries - 1) * getBarGap()) / nSeries;
        return barWidth;
    }

    private SortedSet getCategoriesOnScreen()
    {
        ValueAxis categoryAxis = (ValueAxis) getXAxis();

        Object lowestCategoryShowing = categories.higher((long)categoryAxis.getLowerBound());
        if (lowestCategoryShowing == null)
            return null;

        Object highestCategoryShowing = categories.lower((long)categoryAxis.getUpperBound());
        if (highestCategoryShowing == null)
            return null;

        if((long)highestCategoryShowing < (long)lowestCategoryShowing)
            return null;

        return categories.subSet(lowestCategoryShowing, true, highestCategoryShowing, true);
    }


    /**
     * This is called whenever a series is added or removed and the legend needs to be updated
     */
    @Override
    protected void updateLegend() {
        legend.getItems().clear();
        if (getData() != null) {
            for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {
                Series series = getData().get(seriesIndex);
                Legend.LegendItem legenditem = new Legend.LegendItem(series.getName());
                legenditem.getSymbol().getStyleClass().addAll("chart-bar", "series" + seriesIndex, "bar-legend-symbol",
                        (String) ReflectionUtils.forceFieldCall(Series.class, "defaultColorStyleClass", series));
                legend.getItems().add(legenditem);
            }
        }
        if (legend.getItems().size() > 0) {
            if (getLegend() == null) {
                setLegend(legend);
            }
        } else {
            setLegend(null);
        }
    }

    // -------------- PRIVATE METHODS ------------------------------------------

    private void updateMap(Series series, Data item) {
        final Object category = (orientation == Orientation.VERTICAL) ? item.getXValue() :
                item.getYValue();
        Map<Object, Data<Long, Number>> categoryMap = seriesCategoryMap.get(series);
        if (categoryMap != null) {
            categoryMap.remove(category);
            categories.remove(category);
            if (categoryMap.isEmpty()) seriesCategoryMap.remove(series);
        }
//        if (seriesCategoryMap.isEmpty() && categoryAxis.isAutoRanging()) categoryAxis.getCategories().clear();
    }

    private void animateDataAdd(Data<Long, Number> item, Node bar) {
        double barVal;
        if (orientation == Orientation.VERTICAL) {
            barVal = ((Number) item.getYValue()).doubleValue();
            if (barVal < 0) {
                bar.getStyleClass().add(NEGATIVE_STYLE);
            }
            //item.setCurrentY(getYAxis().toRealValue((barVal < 0) ? -bottomPos : bottomPos));
            ReflectionUtils.forceMethodCall(Data.class, "setCurrentY", item, new Class[]{Object.class}, new Object[]{getYAxis().toRealValue((barVal < 0) ? -bottomPos : bottomPos)});
            getPlotChildren().add(bar);
            item.setYValue(getYAxis().toRealValue(barVal));
//            animate(
//                    new KeyFrame(Duration.ZERO, new KeyValue(item.currentYProperty(),
//                            item.getCurrentY())),
//                    new KeyFrame(Duration.millis(700),
//                            new KeyValue(item.currentYProperty(), item.getYValue(), Interpolator.EASE_BOTH))
//            );
            ReflectionUtils.forceMethodCall(Chart.class, "animate", this, new Object[]{new KeyFrame[]{
                            new KeyFrame(Duration.ZERO, new KeyValue(/*item.currentYProperty(),*/ (ObjectProperty<Number>) ReflectionUtils.forceMethodCall(Data.class, "currentYProperty", item),
									 /*item.getCurrentY()*/ (Number) ReflectionUtils.forceMethodCall(Data.class, "getCurrentY", item))),
                            new KeyFrame(Duration.millis(700),
                                    new KeyValue(/*item.currentYProperty()*/(ObjectProperty<Number>) ReflectionUtils.forceMethodCall(Data.class, "currentYProperty", item), item.getYValue(), Interpolator.EASE_BOTH))}}
            );
        } else {
            barVal = ((Number) item.getXValue()).doubleValue();
            if (barVal < 0) {
                bar.getStyleClass().add(NEGATIVE_STYLE);
            }
            //item.setCurrentX(getXAxis().toRealValue((barVal < 0) ? -bottomPos : bottomPos));
            ReflectionUtils.forceMethodCall(Data.class, "setCurrentX", item, new Class[]{Object.class}, new Object[]{getXAxis().toRealValue((barVal < 0) ? -bottomPos : bottomPos)});
            getPlotChildren().add(bar);
            item.setXValue(getXAxis().toRealValue(barVal));
            ReflectionUtils.forceMethodCall(Chart.class, "animate", this,
                    new KeyFrame(Duration.ZERO, new KeyValue(/*item.currentXProperty(),*/(ObjectProperty<Long>) ReflectionUtils.forceMethodCall(Data.class, "currentXProperty", item),
                            /*item.getCurrentX()*/ (Long) ReflectionUtils.forceMethodCall(Data.class, "getCurrentX", item))),
                    new KeyFrame(Duration.millis(700),
                            new KeyValue(/*item.currentXProperty()*/ (ObjectProperty<Long>) ReflectionUtils.forceMethodCall(Data.class, "currentXProperty", item), item.getXValue(), Interpolator.EASE_BOTH))
            );
        }
    }

    private Timeline createDataRemoveTimeline(final Data<Long, Number> item, final Node bar, final Series<Long, Number> series) {
        Timeline t = new Timeline();
        if (orientation == Orientation.VERTICAL) {
            //            item.setYValue(getYAxis().toRealValue(getYAxis().getZeroPosition()));
            item.setYValue(getYAxis().toRealValue(bottomPos));
            t.getKeyFrames().addAll(new KeyFrame(Duration.ZERO,
                            new KeyValue(/*item.currentYProperty()*/(ObjectProperty<Number>) ReflectionUtils.forceMethodCall(Data.class, "currentYProperty", item), /*item.getCurrentY()*/ (Number) ReflectionUtils.forceMethodCall(Data.class, "getCurrentY", item))),
                    new KeyFrame(Duration.millis(700), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            getPlotChildren().remove(bar);
                            updateMap(series, item);
                        }
                    },
                            new KeyValue(/*item.currentYProperty()*/ (ObjectProperty<Number>) ReflectionUtils.forceMethodCall(Data.class, "currentYProperty", item), item.getYValue(),
                                    Interpolator.EASE_BOTH)));
        } else {
            item.setXValue(getXAxis().toRealValue(getXAxis().getZeroPosition()));
            t.getKeyFrames().addAll(new KeyFrame(Duration.ZERO, new KeyValue(/*item.currentXProperty()*/(ObjectProperty<Long>) ReflectionUtils.forceMethodCall(Data.class, "currentXProperty", item), /*item.getCurrentX()*/ (Long) ReflectionUtils.forceMethodCall(Data.class, "getCurrentX", item))),
                    new KeyFrame(Duration.millis(700), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            getPlotChildren().remove(bar);
                            updateMap(series, item);
                        }
                    },
                            new KeyValue(/*item.currentXProperty()*/ (ObjectProperty<Long>) ReflectionUtils.forceMethodCall(Data.class, "currentXProperty", item), item.getXValue(),
                                    Interpolator.EASE_BOTH)));
        }
        return t;
    }


    private void updateDefaultColorIndex(final Series<Long, Number> series) {
        //int clearIndex = seriesColorMap.get(series);
        Map<Series, Integer> seriesColorMapValue = (Map<Series, Integer>) ReflectionUtils.forceFieldCall(XYChart.class, "seriesColorMap", this);
        int clearIndex = seriesColorMapValue.get(series);

        //colorBits.clear(clearIndex);
        BitSet colorBitsValue = (BitSet) ReflectionUtils.forceFieldCall(XYChart.class, "colorBits", this);

        // DEFAULT_COLOR
        String DEFAULT_COLOR_VALUE = (String) ReflectionUtils.forceFieldCall(XYChart.class, "DEFAULT_COLOR", null);

        for (Data<Long, Number> d : series.getData()) {
            final Node bar = d.getNode();
            if (bar != null) {
                bar.getStyleClass().remove(DEFAULT_COLOR_VALUE + clearIndex);
                colorBitsValue.clear(clearIndex);
            }
        }
        seriesColorMapValue.remove(series);
    }

    private Node createBar(Series series, int seriesIndex, final Data item, int itemIndex) {
        Node bar = item.getNode();
        if (bar == null) {
            bar = new StackPane();
            item.setNode(bar);
        }
        Tooltip tooltip = new Tooltip();
        TooltipContent content = new TooltipContent();
        tooltip.setGraphic(content);
        content.update(((XYBarExtraValues)item.getExtraValue()).getType(),(long)item.getXValue(),((Number)item.getYValue()).longValue());
        Tooltip.install(bar, tooltip);


        String defaultColorStyleClassValue = (String) ReflectionUtils.forceFieldCall(Series.class, "defaultColorStyleClass", series);
        bar.getStyleClass().addAll("chart-bar", "series" + seriesIndex, "data" + itemIndex, /*series.defaultColorStyleClass*/ defaultColorStyleClassValue);
        return bar;
    }

    private Data<Long, Number> getDataItem(Series<Long, Number> series, Object category) {
        Map<Object, Data<Long, Number>> catmap = seriesCategoryMap.get(series);
        return (catmap != null) ? catmap.get(category) : null;
    }

    /** Data extra values for storing close, high and low. */
    public static class XYBarExtraValues {
        private NodeType type;

        public XYBarExtraValues(NodeType type) {
            this.type = type;
        }

        public NodeType getType() {
            return type;
        }
    }

    private static class TooltipContent extends GridPane {
        private Label dateValue = new Label();
        private Label volumeValue = new Label();

        private TooltipContent() {
            Label volume = new Label("VOLUME:");
            getStyleClass().add("tooltip-content");
            setConstraints(dateValue, 0, 0);
            setConstraints(volume,0,1);
            setConstraints(volumeValue,1,1);
            getChildren().addAll(dateValue, volume, volumeValue);
        }

        public void update(NodeType type, long date, long volume) {
            dateValue.setText(DateUtils.formatMonthAbbr(new Date(date)));
            volumeValue.setText(Long.toString(volume));

            switch (type) {
                case BuyOrder:
                    volumeValue.setStyle("-fx-text-fill: #2CFC0E");
                    break;
                case SellOrder:
                    volumeValue.setStyle("-fx-text-fill: red");
                    break;
            }
        }
    }

    // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------

    /**
     * Super-lazy instantiation pattern from Bill Pugh.
     *
     * @treatAsPrivate implementation detail
     */
    private static class StyleableProperties {
        private static final CssMetaData<XYBarChart, Number> BAR_GAP =
                new CssMetaData<XYBarChart, Number>("-fx-bar-gap",
                        SizeConverter.getInstance(), 4.0) {

                    @Override
                    public boolean isSettable(XYBarChart node) {
                        return node.barGap == null || !node.barGap.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(XYBarChart node) {
                        return (StyleableProperty<Number>) node.barGapProperty();
                    }
                };

        private static final CssMetaData<XYBarChart, Number> CATEGORY_GAP =
                new CssMetaData<XYBarChart, Number>("-fx-category-gap",
                        SizeConverter.getInstance(), 10.0) {

                    @Override
                    public boolean isSettable(XYBarChart node) {
                        return node.categoryGap == null || !node.categoryGap.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(XYBarChart node) {
                        return (StyleableProperty<Number>) node.categoryGapProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {

            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<CssMetaData<? extends Styleable, ?>>(XYChart.getClassCssMetaData());
            styleables.add(BAR_GAP);
            styleables.add(CATEGORY_GAP);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     *         CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     *
     * @since JavaFX 8.0
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * Pseudoclass indicating this is a vertical chart.
     */
    private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE =
            PseudoClass.getPseudoClass("vertical");

    /**
     * Pseudoclass indicating this is a horizontal chart.
     */
    private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE =
            PseudoClass.getPseudoClass("horizontal");

}