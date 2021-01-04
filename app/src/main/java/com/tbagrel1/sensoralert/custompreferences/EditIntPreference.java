package com.tbagrel1.sensoralert.custompreferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import com.tbagrel1.sensoralert.R;

/**
 * Custom preference element used to set an int value.
 */
public class EditIntPreference extends DialogPreference {
    public static final String LOG_TAG = "EditIntPreference";
    public static final int FALLBACK_DEFAULT_VALUE = 0;
    public static final int FALLBACK_MIN_VALUE = 0;
    public static final int FALLBACK_MAX_VALUE = 1000;

    private Integer volatileValue = null;
    private int minValue;
    private int maxValue;

    public EditIntPreference(Context context) {
        super(context);
    }

    public EditIntPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMinMaxValuesFrom(context, attrs);
    }

    private void setMinMaxValuesFrom(Context context, AttributeSet attrs) {
        // Get min and max values from XML attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditIntPreference, 0, 0);
        String minValueString = a.getString(R.styleable.EditIntPreference_minValue);
        minValue = FALLBACK_MIN_VALUE;
        try {
            minValue = Integer.parseInt(minValueString);
            Log.i(LOG_TAG, String.format("Extracted min value: %d", minValue));
        } catch (Exception e) {
            Log.e(
                LOG_TAG,
                String.format("Invalid min value: %s. Using fallback value instead.", e.toString())
            );
        }
        String maxValueString = a.getString(R.styleable.EditIntPreference_maxValue);
        maxValue = FALLBACK_MAX_VALUE;
        try {
            maxValue = Integer.parseInt(maxValueString);
            Log.i(LOG_TAG, String.format("Extracted max value: %d", maxValue));
        } catch (Exception e) {
            Log.e(
                LOG_TAG,
                String.format("Invalid max value: %s. Using fallback value instead.", e.toString())
            );
        }
        a.recycle();
    }

    public EditIntPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setMinMaxValuesFrom(context, attrs);
    }

    public EditIntPreference(
        Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setMinMaxValuesFrom(context, attrs);
    }

    protected int getPersistedValue() {
        int defaultValue = volatileValue == null ?
                           FALLBACK_DEFAULT_VALUE :
                           volatileValue; // volatileValue should be set at this point
        return getPersistedInt(defaultValue);
    }

    protected boolean persistValue(int value) {
        return setValue(value);
    }

    private boolean setValue(int value) {
        volatileValue = value;
        setSummary(String.valueOf(value));
        boolean res = persistInt(value);
        notifyChanged();
        return res;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Parses the default value specified as a XML key
        String valueString = a.getString(index);
        int defaultValue = FALLBACK_DEFAULT_VALUE;
        try {
            defaultValue = Integer.parseInt(valueString);
            Log.i(LOG_TAG, String.format("Extracted default value: %d", defaultValue));
        } catch (Exception e) {
            Log.e(LOG_TAG, String.format(
                "Invalid default value (not a valid number): %s. Using fallback value instead.",
                e.toString()
            ));
        }
        return defaultValue;
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        int intDefaultValue = defaultValue == null ? FALLBACK_DEFAULT_VALUE : (int) defaultValue;
        setValue(getPersistedInt(intDefaultValue));
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }
}
