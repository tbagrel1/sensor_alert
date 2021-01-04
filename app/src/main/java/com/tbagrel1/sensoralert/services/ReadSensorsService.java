package com.tbagrel1.sensoralert.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.tbagrel1.sensoralert.PreferencesWrapper;
import com.tbagrel1.sensoralert.dbmodels.LightDataPoint;
import com.tbagrel1.sensoralert.dbmodels.Reading;
import com.tbagrel1.sensoralert.dbmodels.ReadingWithLightDataPoints;
import com.tbagrel1.sensoralert.dbmodels.SensorAlertDao;
import com.tbagrel1.sensoralert.dbmodels.SensorAlertDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service used to read the sensors values on the API.
 */
public class ReadSensorsService extends IntentService {
    public static final String LOG_TAG = "ReadSensorsService";
    public static final int REQUEST_CODE_PERIODIC = 1999;

    private static final String ACTION_READ_SENSORS_AND_UPDATE =
        "com.tbagrel1.sensoralert.action.read_sensors_and_update";
    private static final int TIMEOUT = 5000;
    private static final int NO_FLAGS = 0;

    private static PendingIntent readSensorsPendingIntent = null;

    /**
     * Static method allowing to stop the periodic execution of this service.
     *
     * @param context application context
     */
    public static void stopRefreshingSensors(Context context) {
        if (!isPeriodicExecutionProgrammed()) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(readSensorsPendingIntent);
        readSensorsPendingIntent = null;
    }

    /**
     * Returns true iif the periodic execution of this service is programmed.
     *
     * @return true iif the periodic execution of this service is programmed
     */
    public static boolean isPeriodicExecutionProgrammed() {
        return readSensorsPendingIntent != null;
    }

    /**
     * Static method allowing to start the periodic execution of this service.
     *
     * @param context application context
     */
    public static void startRefreshingSensors(Context context) {
        if (isPeriodicExecutionProgrammed()) {
            return;
        }
        PreferencesWrapper preferences = PreferencesWrapper.get(context);
        int refreshRate = preferences.getInt("refresh_rate");
        Intent intent = ReadSensorsService.getReadSensorsIntent(context);
        readSensorsPendingIntent = PendingIntent.getService(context,
            ReadSensorsService.REQUEST_CODE_PERIODIC,
            intent,
            NO_FLAGS
        );
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + refreshRate * 1000,
            refreshRate * 1000L,
            readSensorsPendingIntent
        );
    }

    /**
     * Creates a configured intent asking this service to read the sensors values.
     *
     * @param context application context
     * @return a configured intent
     */
    public static Intent getReadSensorsIntent(Context context) {
        Intent intent = new Intent(context, ReadSensorsService.class);
        intent.setAction(ACTION_READ_SENSORS_AND_UPDATE);
        return intent;
    }

    private SensorAlertDatabase db;

    public ReadSensorsService() {
        super("ReadSensorsService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = SensorAlertDatabase.getInstance(getApplicationContext());
    }

    /**
     * Trigger the correct action depending on the received intent.
     *
     * @param intent the received intent.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_READ_SENSORS_AND_UPDATE.equals(action)) {
                PreferencesWrapper preferences = PreferencesWrapper.get(getApplicationContext());
                int keepDataFor = preferences.getInt("keep_data_for");
                String apiRootUrl = preferences.getString("api_root_url");
                handleActionReadSensorsAndUpdate(keepDataFor, apiRootUrl);
            }
        }
    }

    /**
     * Reads the sensors values, updates the database and call the next service.
     *
     * @param keepDataFor how long should the light data be kept for in the database
     * @param apiRootUrl  root url of the API (http://{host}:{port})
     */
    private void handleActionReadSensorsAndUpdate(int keepDataFor, String apiRootUrl) {
        // 1. Delete records from more than xx days in the DB
        Log.i(LOG_TAG, "Will delete the old records");
        List<Reading> oldReadings = db.dao().getOldReadings(SensorAlertDao.pastLimit(keepDataFor));
        db.dao().deleteAllReadings(oldReadings);
        // 2. Read sensors values and write the new records in the DB
        Log.i(LOG_TAG, "Will read sensors data from the API and update the DB");
        ReadingWithLightDataPoints prevSuccessfulReading =
            db.dao().getLatestSuccessfulReadingWithLightDataPoints();
        ReadingWithLightDataPoints newReading = readSensorsAndPopulateDb(apiRootUrl);
        // 3. Create intent to call AlertService, with params prevId and newId and send it with
        // startService(intent)
        if (newReading.reading.isSuccess) {
            Log.i(LOG_TAG, "New data read successfully");
            if (prevSuccessfulReading != null) {
                Log.i(
                    LOG_TAG,
                    "Previous data exists, so I will be able to compare the previous and new datasets. Calling AlertService..."
                );
                Intent alert = AlertService.getReadNewDataAndAlertIntent(getApplicationContext(),
                    prevSuccessfulReading.reading.id,
                    newReading.reading.id
                );
                startService(alert);
            } else {
                Log.i(
                    LOG_TAG,
                    "Previous data doesn't exist, so I won't be able to detect any light switches. Not calling AlertService."
                );
            }
        } else {
            Log.i(
                LOG_TAG,
                "Reading new data threw an error, so I'm calling AlertService to notify the user of the error encountered"
            );
            Intent alert = AlertService.getReadFailureAndAlertIntent(getApplicationContext(),
                newReading.reading.id
            );
            startService(alert);
        }
    }

    /**
     * Reads the sensors values and updates the database.
     *
     * @param apiRootUrl root url of the API (http://{host}:{port})
     * @return the new reading data (after ID auto-generation)
     */
    private ReadingWithLightDataPoints readSensorsAndPopulateDb(String apiRootUrl) {
        int n = apiRootUrl.length();
        if (apiRootUrl.charAt(n - 1) == '/') {
            apiRootUrl = apiRootUrl.substring(0, n - 1);
        }
        String url = String.format("%s/iotlab/rest/data/1/light1/last", apiRootUrl);

        int httpStatusCode = 0;
        String errorMessage = "";
        boolean isSuccess = true;
        List<LightDataPoint> points = new ArrayList<>();
        try {
            Log.i(LOG_TAG, String.format("Calling GET %s to get new sensors data...", url));
            HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            httpStatusCode = connection.getResponseCode();
            if (httpStatusCode / 100 != 2) {
                throw new Exception(new BufferedReader(new InputStreamReader(connection.getErrorStream()))
                    .lines()
                    .collect(Collectors.joining("\n")));
            }
            String content =
                new BufferedReader(new InputStreamReader(connection.getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            try {
                Log.i(LOG_TAG, "Data received from the sensors API. Trying to parse it...");
                points = parseJsonString(content);
                Log.i(LOG_TAG, "Data parsed successfully");
            } catch (Exception e) {
                points.clear();
                throw new Exception("Malformed/Unexpected data sent back by the server: " + e);
            }
        } catch (Exception e) {
            errorMessage = e.toString();
            Log.e(LOG_TAG, errorMessage);
            isSuccess = false;
        }
        Reading reading =
            new Reading(OffsetDateTime.now(), isSuccess, url, httpStatusCode, errorMessage);
        return db.dao()
            .insertReadingWithLightDataPoints(new ReadingWithLightDataPoints(reading, points));
    }

    /**
     * Parses the raw JSON string returned by the API into a list of light data points.
     *
     * @param content the raw JSON string returned by the API
     * @return the corresponding list of light data points
     * @throws Exception if the JSON string cannot be parsed
     */
    private List<LightDataPoint> parseJsonString(String content) throws Exception {
        List<LightDataPoint> points = new ArrayList<>();
        JSONObject root = new JSONObject(content);
        JSONArray data = root.getJSONArray("data");
        for (int i = 0; i < data.length(); ++i) {
            JSONObject measure = data.getJSONObject(i);
            long timestamp = measure.getLong("timestamp");
            String moteId = measure.getString("mote");
            double value = measure.getDouble("value");
            OffsetDateTime instant =
                Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            points.add(new LightDataPoint(instant, 0, moteId, value));
        }
        return points;
    }
}
