package com.tbagrel1.sensoralert.fragments;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

/**
 * Helper class with methods related to fragment management.
 */
public class FragmentUtils {
    /**
     * Hides the touch keyboard if it is open.
     *
     * @param fragment the currently displayed fragment
     */
    public static void hideKeyboard(Fragment fragment) {
        Activity activity = fragment.getActivity();
        InputMethodManager inputMethodManager =
            (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS
            );
        }
    }
}
