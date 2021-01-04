package com.tbagrel1.sensoralert.dbmodels;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Dao
public interface SensorAlertDao {
    /**
     * Static method used to generated the correct SQLite date string part to represent a date
     * subtraction.
     *
     * @param days number of days in the past
     * @return the corresponding SQLite date string part
     */
    static String pastLimit(int days) {
        return String.format("-%d days", days);
    }

    @Query("SELECT * FROM readings")
    List<Reading> getAllReadings();

    @Query("SELECT * FROM readings WHERE id = (:id)")
    Reading getReading(long id);

    @Query("SELECT * FROM readings WHERE datetime(instant) <= datetime('now', (:pastLimit))")
    List<Reading> getOldReadings(String pastLimit);

    @Transaction
    @Query("SELECT * FROM readings WHERE is_success = 1 ORDER BY datetime(instant) DESC LIMIT 1")
    ReadingWithLightDataPoints getLatestSuccessfulReadingWithLightDataPoints();

    @Transaction
    @Query("SELECT * FROM readings ORDER BY datetime(instant) DESC LIMIT 1")
    ReadingWithLightDataPoints getLatestReadingWithLightDataPoints();

    @Transaction
    @Query("SELECT * FROM readings ORDER BY datetime(instant) DESC LIMIT 1")
    LiveData<ReadingWithLightDataPoints> liveLatestReadingWithLightDataPoints();

    @Transaction
    @Query("SELECT * FROM readings WHERE is_success = 1 ORDER BY datetime(instant) DESC LIMIT 1")
    LiveData<ReadingWithLightDataPoints> liveLatestSuccessfulReadingWithLightDataPoints();

    @Insert
    long[] insertAllReadings(Collection<Reading> readings);

    @Insert
    long insertLightDataPoint(LightDataPoint lightDataPoint);

    default Collection<ReadingWithLightDataPoints> insertAllReadingsWithLightDataPoints(
        Collection<ReadingWithLightDataPoints> readingsWithLightDataPoints
    ) {
        return readingsWithLightDataPoints.stream()
            .map(this::insertReadingWithLightDataPoints)
            .collect(Collectors.toList());
    }

    @Transaction
    default ReadingWithLightDataPoints insertReadingWithLightDataPoints(
        ReadingWithLightDataPoints readingWithLightDataPoints
    ) {
        long readingId = insertReading(readingWithLightDataPoints.reading);
        for (LightDataPoint point : readingWithLightDataPoints.points) {
            point.readingId = readingId;
        }
        insertAllLightDataPoints(readingWithLightDataPoints.points);
        return getReadingWithLightDataPoints(readingId);
    }

    @Insert
    long insertReading(Reading reading);

    @Insert
    long[] insertAllLightDataPoints(Collection<LightDataPoint> lightDataPoints);

    @Transaction
    @Query("SELECT * FROM readings WHERE id = (:id)")
    ReadingWithLightDataPoints getReadingWithLightDataPoints(long id);

    @Delete
    void deleteReading(Reading reading);

    @Delete
    void deleteAllReadings(Collection<Reading> readings);

    @Delete
    void deleteLightDataPoint(LightDataPoint lightDataPoint);

    @Delete
    void deleteAllLightDataPoints(Collection<LightDataPoint> lightDataPoints);

    /**
     * Apply a mote alias
     *
     * @param moteAlias the mote alias to apply
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void alias(MoteAlias moteAlias);

    /**
     * Returns the current mote alias for the specified mote. Returns null if no alias has been
     * set.
     *
     * @param moteId ID of the mote to fetch the alias of
     * @return the current mote alias, or null if no alias has been set
     */
    @Query("SELECT name FROM mote_aliases WHERE id = (:moteId)")
    String getAlias(String moteId);

    /**
     * Returns the mote alias for the specified mote as a LiveData.
     *
     * @param moteId ID of the mote to fetch the alias of
     * @return a LiveData representing the mote alias
     */
    @Query("SELECT name FROM mote_aliases WHERE id = (:moteId)")
    LiveData<String> liveAlias(String moteId);

    /**
     * Remove the specified moteAlias from the database.
     *
     * @param moteAlias the mote alias to remove
     */
    @Delete
    void unalias(MoteAlias moteAlias);

    /**
     * Returns all the light data points corresponding to the specified period
     *
     * @param beginDateString begin date of the period
     * @param endDateString   end date of the period
     * @return all the light data points corresponding to the specified period
     */
    @Query(
        "SELECT * FROM light_data_points WHERE datetime(instant) >= datetime(:beginDateString) AND datetime(instant) <= datetime(:endDateString)")
    List<LightDataPoint> exportLightDataPoints(String beginDateString, String endDateString);

    /**
     * Returns the chart points corresponding to all the light data measures of the specified mote
     * in the last 24 hours.
     *
     * @param moteId ID of the mote to fetch the data of
     * @return the chart points corresponding to all the light data measures of the specified mote
     * in the last 24 hours
     */
    @Query(
        "SELECT DISTINCT instant, value FROM light_data_points WHERE mote_id = (:moteId) AND datetime(instant) >= datetime('now', '-1 days')")
    List<ChartPoint> getChartPoints(String moteId);
}
