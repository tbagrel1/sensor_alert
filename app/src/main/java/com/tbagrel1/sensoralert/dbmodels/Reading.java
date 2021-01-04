package com.tbagrel1.sensoralert.dbmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class representing a attempt to read sensors data from the API.
 */
@Entity(tableName = "readings")
public class Reading {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "instant")
    @NonNull
    public OffsetDateTime instant;

    @ColumnInfo(name = "is_success")
    public boolean isSuccess;

    @ColumnInfo(name = "url")
    @NonNull
    public String url;

    @ColumnInfo(name = "http_status_code")
    public int httpStatusCode;

    @ColumnInfo(name = "error_message")
    @Nullable
    public String errorMessage;

    public Reading(
        long id,
        @NonNull OffsetDateTime instant,
        boolean isSuccess,
        @NonNull String url,
        int httpStatusCode,
        @Nullable String errorMessage
    ) {
        this.id = id;
        this.instant = instant;
        this.isSuccess = isSuccess;
        this.url = url;
        this.httpStatusCode = httpStatusCode;
        this.errorMessage = errorMessage;
    }

    @Ignore
    public Reading(
        @NonNull OffsetDateTime instant,
        boolean isSuccess,
        @NonNull String url,
        int httpStatusCode,
        @Nullable String errorMessage
    ) {
        this.instant = instant;
        this.isSuccess = isSuccess;
        this.url = url;
        this.httpStatusCode = httpStatusCode;
        this.errorMessage = errorMessage;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
            "Reading#%d { instant = %s, isSuccess = %s, url = %s, httpStatusCode = %d, errorMessage = %s }",
            id,
            instant.format(DateTimeFormatter.RFC_1123_DATE_TIME),
            isSuccess ? "true" : "false",
            url,
            httpStatusCode,
            errorMessage == null ? "null" : errorMessage
        );
    }
}
