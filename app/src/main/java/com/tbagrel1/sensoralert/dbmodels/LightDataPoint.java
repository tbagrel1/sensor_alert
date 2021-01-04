package com.tbagrel1.sensoralert.dbmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Class representing a light value measure realized at a specific instant by a specific mote.
 */
@Entity(tableName = "light_data_points", foreignKeys = {
    @ForeignKey(onDelete = CASCADE,
                entity = Reading.class,
                parentColumns = "id",
                childColumns = "reading_id")
})
public class LightDataPoint {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "instant")
    @NonNull
    public OffsetDateTime instant;

    @ColumnInfo(name = "reading_id")
    public long readingId;

    @ColumnInfo(name = "mote_id")
    @NonNull
    public String moteId;

    @ColumnInfo(name = "value")
    public double value;

    public LightDataPoint(
        long id,
        @NonNull OffsetDateTime instant,
        long readingId,
        @NonNull String moteId,
        double value
    ) {
        this.id = id;
        this.instant = instant;
        this.readingId = readingId;
        this.moteId = moteId;
        this.value = value;
    }

    @Ignore
    public LightDataPoint(
        @NonNull OffsetDateTime instant, long readingId, @NonNull String moteId, double value
    ) {
        this.instant = instant;
        this.readingId = readingId;
        this.moteId = moteId;
        this.value = value;
    }

    public boolean isOn(double lightSwitchThreshold) {
        return this.value > lightSwitchThreshold;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
            "LightDataPoint#%d { instant = %s, readingId = %d, moteId = %s, value = %.2f }",
            id,
            instant.format(DateTimeFormatter.RFC_1123_DATE_TIME),
            readingId,
            moteId,
            value
        );
    }

    /**
     * Returns the JSON representation of this light data point. Used for export purposes.
     *
     * @return a JSON object representing this light data point
     */
    public String toSerialized() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", String.valueOf(id));
            json.put("instant", instant.toString());
            json.put("readingId", String.valueOf(id));
            json.put("moteId", moteId);
            json.put("value", value);
            return json.toString();
        } catch (JSONException e) {
            Log.e(
                "LightDataPoint",
                String.format("Unable to serialize LightDataPoint: %s", e.toString())
            );
            return "{}";
        }
    }
}
