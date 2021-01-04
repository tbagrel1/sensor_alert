package com.tbagrel1.sensoralert.custompreferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// This class is used in our preference where user can pick a time for notifications to appear.
// Specifically, this class is responsible for saving/retrieving preference data.

/**
 * Custom preference element used to set a time.
 */
public class TimePickerPreference extends DialogPreference {
    public static final String LOG_TAG = "TimePickerPreference";
    public static final int FALLBACK_DEFAULT_MINUTES_FROM_MIDNIGHT = 0;
    private Integer volatileMinutesFromMidnight = null;

    public TimePickerPreference(Context context) {
        super(context);
    }

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimePickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePickerPreference(
        Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getPersistedMinutesFromMidnight() {
        int defaultMinutesFromMidnight = volatileMinutesFromMidnight == null ?
                                         FALLBACK_DEFAULT_MINUTES_FROM_MIDNIGHT :
                                         volatileMinutesFromMidnight; // should be set at this point
        return getPersistedInt(defaultMinutesFromMidnight);
    }

    public boolean persistMinutesFromMidnight(int minutesFromMidnight) {
        return setMinutesFromMidnight(minutesFromMidnight);
    }

    private boolean setMinutesFromMidnight(int minutesFromMidnight) {
        volatileMinutesFromMidnight = minutesFromMidnight;
        setSummary(minutesFromMidnightToHourlyTime(minutesFromMidnight));
        boolean res = persistInt(minutesFromMidnight);
        notifyChanged();
        return res;
    }

    public static String minutesFromMidnightToHourlyTime(int minutesFromMidnight) {
        return String.format("%02d:%02d", minutesFromMidnight / 60, minutesFromMidnight % 60);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Parses the default value specified as a XML key
        String hourString = a.getString(index);
        int defaultMinutesFromMidnight = FALLBACK_DEFAULT_MINUTES_FROM_MIDNIGHT;
        try {
            LocalTime time = LocalTime.parse(hourString, DateTimeFormatter.ofPattern("HH:mm"));
            defaultMinutesFromMidnight = time.getHour() * 60 + time.getMinute();
            Log.i(LOG_TAG,
                String.format("Extracted default value: %d", defaultMinutesFromMidnight)
            );
        } catch (Exception e) {
            Log.e(LOG_TAG, String.format(
                "Invalid default value (not a valid hour): %s. Using fallback instead.",
                e.toString()
            ));
        }
        return defaultMinutesFromMidnight;
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        int intDefaultValue =
            defaultValue == null ? FALLBACK_DEFAULT_MINUTES_FROM_MIDNIGHT : (int) defaultValue;
        setMinutesFromMidnight(getPersistedInt(intDefaultValue));
    }
}
