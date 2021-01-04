package com.tbagrel1.sensoralert.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;

import com.tbagrel1.sensoralert.MainActivity;
import com.tbagrel1.sensoralert.R;
import com.tbagrel1.sensoralert.custompreferences.EditIntPreference;
import com.tbagrel1.sensoralert.custompreferences.EditIntPreferenceDialog;
import com.tbagrel1.sensoralert.custompreferences.TimePickerPreference;
import com.tbagrel1.sensoralert.custompreferences.TimePickerPreferenceDialog;

/**
 * Fragment used to display the preference window. See the xml/preferences.xml layout.
 */
public class PreferencesFragment extends PreferenceFragmentCompat
    implements DisplayedAwareFragment {
    public static final String LOG_TAG = "PreferencesFragment";

    @Override
    public void onDetach() {
        super.onDetach();
        FragmentUtils.hideKeyboard(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    /**
     * Set the title when this fragment is displayed.
     */
    @Override
    public void onDisplay() {
        Log.i(LOG_TAG, "Displayed");
        ((MainActivity) getActivity()).setTitleBar("Settings", true);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        Log.i(LOG_TAG, "PreferencesFragment starting up...");
        setHasOptionsMenu(true);
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getContext().getColor(R.color.white));
        return view;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Special handling is required for custom preferences items and dialogs.
        if (preference instanceof TimePickerPreference) {
            PreferenceDialogFragmentCompat dialogFragment =
                TimePickerPreferenceDialog.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), null);
        } else if (preference instanceof EditIntPreference) {
            PreferenceDialogFragmentCompat dialogFragment =
                EditIntPreferenceDialog.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
