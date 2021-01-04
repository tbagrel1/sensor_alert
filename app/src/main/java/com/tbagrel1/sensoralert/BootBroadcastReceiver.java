package com.tbagrel1.sensoralert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tbagrel1.sensoralert.services.ReadSensorsService;

/**
 * Broadcast receiver used to program the periodic execution of the ReadSensors service on boot.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    public static final String LOG_TAG = "BootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(
            LOG_TAG,
            "Boot notification received, programming the periodic execution of the ReadSensors service..."
        );
        ReadSensorsService.startRefreshingSensors(context);
    }
}
