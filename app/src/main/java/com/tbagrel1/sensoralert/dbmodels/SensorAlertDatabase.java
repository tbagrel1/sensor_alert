package com.tbagrel1.sensoralert.dbmodels;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Singleton class representing the SQLite database handle.
 */
@Database(entities = {Reading.class, LightDataPoint.class, MoteAlias.class}, version = 4)
@TypeConverters({OffsetDateTimeTypeConverter.class})
public abstract class SensorAlertDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "sensor_alert";
    private static SensorAlertDatabase instance = null;

    public static SensorAlertDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, SensorAlertDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .enableMultiInstanceInvalidation()
                .build();
        }
        return instance;
    }

    public abstract SensorAlertDao dao();
}
