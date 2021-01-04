package com.tbagrel1.sensoralert.dbmodels;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * Composite class used to retrieve a reading with the associated light data points with the Room
 * DAO.
 */
public class ReadingWithLightDataPoints {
    @Embedded
    public Reading reading;

    @Relation(parentColumn = "id", entityColumn = "reading_id")
    public List<LightDataPoint> points;

    public ReadingWithLightDataPoints(Reading reading, List<LightDataPoint> points) {
        this.reading = reading;
        this.points = points;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
            "ReadingWithLightDataPoints { reading = %s, point = %s }",
            reading.toString(),
            points.toString()
        );
    }
}
