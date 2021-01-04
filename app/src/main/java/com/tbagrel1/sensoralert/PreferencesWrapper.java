package com.tbagrel1.sensoralert;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Class used to log any value read in the preferences for debugging purposes.
 */
public class PreferencesWrapper {
    public static final String LOG_TAG = "PreferencesWrapper";

    public static final int FALLBACK_INT = 9911;
    public static final String FALLBACK_STRING = "9911";
    public static final boolean FALLBACK_BOOLEAN = false;

    public static PreferencesWrapper get(Context context) {
        return new PreferencesWrapper(context);
    }

    private final SharedPreferences preferences;

    private PreferencesWrapper(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getInt(String key) {
        int value = preferences.getInt(key, FALLBACK_INT);
        Log.i(LOG_TAG, String.format("Read \"%s\": %d", key, value));
        return value;
    }

    public boolean getBoolean(String key) {
        boolean value = preferences.getBoolean(key, FALLBACK_BOOLEAN);
        Log.i(LOG_TAG, String.format("Read \"%s\": %s", key, value ? "true" : "false"));
        return value;
    }

    public String getString(String key) {
        String value = preferences.getString(key, FALLBACK_STRING);
        Log.i(LOG_TAG, String.format("Read \"%s\": %s", key, value));
        return value;
    }
}
