package components;

import graph.ChartPanZoomManager;
import javafx.event.EventHandler;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by gavintam on 23/05/15.
 */
public class DateRangeFilterBuilder {
    private enum DateType{
        Start, End
    }
    public static void addDateFilters(HBox selector, XYChart chart) {
        DatePicker startDatePicker = new DatePicker();
        configureDatePicker(startDatePicker, chart, DateType.Start);
        LabelledSelector startDateSelector = new LabelledSelector("Start Date:", startDatePicker);
        selector.getChildren().add(startDateSelector);

        DatePicker endDatePicker = new DatePicker();
        configureDatePicker(endDatePicker, chart, DateType.End);
        LabelledSelector endDateSelector = new LabelledSelector("End Date:", endDatePicker);
        selector.getChildren().add(endDateSelector);

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

    private static void configureDatePicker(DatePicker datePicker, XYChart chart, DateType type) {
        datePicker.getStyleClass().add("datepicker");
        datePicker.setPromptText("dd/MM/yyyy");
        datePicker.setConverter(new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
        if (type.equals(DateType.Start)) {
            datePicker.setOnAction(new EventHandler() {
                public void handle(javafx.event.Event t) {
                    LocalDate date = datePicker.getValue();
                    if (date == null) return;
                    long startDate = date.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    ChartPanZoomManager.updateLowerBound(chart, startDate);
                }
            });
        } else {
            datePicker.setOnAction(new EventHandler() {
                public void handle(javafx.event.Event t) {
                    LocalDate date = datePicker.getValue();
                    if (date == null) return;
                    long endDate = date.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    ChartPanZoomManager.updateUpperBound(chart, endDate);
                }
            });
        }
    }

}
