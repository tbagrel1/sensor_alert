package com.tbagrel1.sensoralert.dbmodels;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

import com.anychart.chart.common.dataentry.ValueDataEntry;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class used to receive results from the chart DAO query. Can be converted to a ValueDataEntry to
 * be compatible with the AnyChart library.
 */
public class ChartPoint {
    @ColumnInfo(name = "instant")
    @NonNull
    public OffsetDateTime instant;

    @ColumnInfo(name = "value")
    public double value;

    public ChartPoint(@NonNull OffsetDateTime instant, double value) {
        this.instant = instant;
        this.value = value;
    }

    /**
     * Converts this chart point in a format compatible with AnyChart.
     *
     * @return an AnyChart compatible data point
     */
    public ValueDataEntry toAnyChartPoint() {
        return new ValueDataEntry(instant.toString(), value);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
            "ChartPoint { instant = %s, value = %.2f }",
            instant.format(DateTimeFormatter.RFC_1123_DATE_TIME),
            value
        );
    }
}
