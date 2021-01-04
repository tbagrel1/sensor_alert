package com.tbagrel1.sensoralert.fragments;

/**
 * Interface used to created a hook on each fragment when it is push on the top of the fragment
 * stack.
 */
public interface DisplayedAwareFragment {
    void onDisplay();
}
