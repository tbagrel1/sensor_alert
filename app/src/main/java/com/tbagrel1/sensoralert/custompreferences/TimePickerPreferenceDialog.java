package com.tbagrel1.sensoralert.custompreferences;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.PreferenceDialogFragmentCompat;

/**
 * Custom preference dialog used to set a time through a TimePicker. Used in TimePickerPreference.
 */
public class TimePickerPreferenceDialog extends PreferenceDialogFragmentCompat {

    public static TimePickerPreferenceDialog newInstance(String key) {
        TimePickerPreferenceDialog fragment = new TimePickerPreferenceDialog();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    private TimePicker timePicker;

    @Override
    protected View onCreateDialogView(Context context) {
        timePicker = new TimePicker(context);
        return timePicker;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        int minutesFromMidnight =
            ((TimePickerPreference) getPreference()).getPersistedMinutesFromMidnight();
        timePicker.setIs24HourView(true);
        timePicker.setHour(minutesFromMidnight / 60);
        timePicker.setMinute(minutesFromMidnight % 60);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int minutesFromMidnight = (timePicker.getHour() * 60) + (timePicker.getMinute());
            ((TimePickerPreference) getPreference()).persistMinutesFromMidnight(minutesFromMidnight);
        }
    }
}
