package com.tbagrel1.sensoralert.custompreferences;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.preference.PreferenceDialogFragmentCompat;

/**
 * Custom preference dialog used to set an int value through a NumberPicker. Used in
 * EditIntPreference.
 */
public class EditIntPreferenceDialog extends PreferenceDialogFragmentCompat {

    public static EditIntPreferenceDialog newInstance(String key) {
        EditIntPreferenceDialog fragment = new EditIntPreferenceDialog();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    private NumberPicker numberPicker;

    @Override
    protected View onCreateDialogView(Context context) {
        numberPicker = new NumberPicker(context);
        return numberPicker;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditIntPreference preference = (EditIntPreference) getPreference();
        numberPicker.setMinValue(preference.getMinValue());
        numberPicker.setMaxValue(preference.getMaxValue());
        numberPicker.setValue(preference.getPersistedValue());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int value = numberPicker.getValue();
            ((EditIntPreference) getPreference()).persistValue(value);
        }
    }
}
