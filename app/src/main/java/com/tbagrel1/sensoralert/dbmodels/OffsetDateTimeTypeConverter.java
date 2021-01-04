package com.tbagrel1.sensoralert.dbmodels;

import androidx.room.TypeConverter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converters allowing to use the OffsetDateTime type with the Room (SQLite) database.
 */
public class OffsetDateTimeTypeConverter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @TypeConverter
    public static OffsetDateTime toOffsetDateTime(String value) {
        return formatter.parse(value, OffsetDateTime::from);
    }

    @TypeConverter
    public static String fromOffsetDateTime(OffsetDateTime date) {
        return date.format(formatter);
    }
}
