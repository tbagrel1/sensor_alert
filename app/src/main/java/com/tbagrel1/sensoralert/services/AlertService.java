package com.tbagrel1.sensoralert.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.tbagrel1.sensoralert.EmailHandling;
import com.tbagrel1.sensoralert.PreferencesWrapper;
import com.tbagrel1.sensoralert.R;
import com.tbagrel1.sensoralert.dbmodels.LightDataPoint;
import com.tbagrel1.sensoralert.dbmodels.PrevNewPoint;
import com.tbagrel1.sensoralert.dbmodels.Reading;
import com.tbagrel1.sensoralert.dbmodels.ReadingWithLightDataPoints;
import com.tbagrel1.sensoralert.dbmodels.SensorAlertDatabase;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;

import static com.tbagrel1.sensoralert.dbmodels.PrevNewPoint.SWITCHED_ON;

/**
 * Service used to alert the user when light switches or reading errors occurs.
 */
public class AlertService extends IntentService {
    public static final String LOG_TAG = "AlertService";

    private static final String ACTION_READ_NEW_DATA_AND_ALERT =
        "com.tbagrel1.sensoralert.action.read_new_data_and_alert";
    private static final String ACTION_READ_FAILURE_AND_ALERT =
        "com.tbagrel1.sensoralert.action.read_failure_and_alert";

    private static final String EXTRA_PREV_READING_ID =
        "com.tbagrel1.sensoralert.extra.prev_reading_id";
    private static final String EXTRA_NEW_READING_ID =
        "com.tbagrel1.sensoralert.extra.new_reading_id";

    private static final String NOTIFICATION_CHANNEL_ID =
        "com.tbagrel1.sensoralert.notification.alert";

    /**
     * Creates an intent to triggers this service and ask to read the new available data and alert
     * if necessary.
     *
     * @param context       application context
     * @param prevReadingId ID of the previous reading (to compare states)
     * @param newReadingId  ID of the new reading (to compare states)
     * @return the configured intent
     */
    public static Intent getReadNewDataAndAlertIntent(
        Context context, long prevReadingId, long newReadingId
    ) {
        Intent intent = new Intent(context, AlertService.class);
        intent.setAction(ACTION_READ_NEW_DATA_AND_ALERT);
        intent.putExtra(EXTRA_PREV_READING_ID, prevReadingId);
        intent.putExtra(EXTRA_NEW_READING_ID, newReadingId);
        return intent;
    }

    /**
     * Creates an intent to triggers this service and ask to read the error message and alert if
     * necessary.
     *
     * @param context      application context
     * @param newReadingId ID of the failed reading
     * @return the configured intent
     */
    public static Intent getReadFailureAndAlertIntent(Context context, long newReadingId) {
        Intent intent = new Intent(context, AlertService.class);
        intent.setAction(ACTION_READ_FAILURE_AND_ALERT);
        intent.putExtra(EXTRA_NEW_READING_ID, newReadingId);
        return intent;
    }
    private Session emailSession;
    private SensorAlertDatabase db;
    private NotificationManager notificationManager;
    public AlertService() {
        super("AlertService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        emailSession =
            EmailHandling.getEmailSession(PreferencesWrapper.get(getApplicationContext()));
        db = SensorAlertDatabase.getInstance(getApplicationContext());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
            if (ACTION_READ_NEW_DATA_AND_ALERT.equals(action)) {
                PreferencesWrapper preferences = PreferencesWrapper.get(getApplicationContext());
                double lightSwitchThreshold = preferences.getInt("light_switch_threshold");
                long prevReadingId = intent.getLongExtra(EXTRA_PREV_READING_ID, -1);
                long newReadingId = intent.getLongExtra(EXTRA_NEW_READING_ID, -1);
                AlertSettings alertSettings = new AlertSettings(preferences);
                handleActionReadNewDataAndAlert(lightSwitchThreshold,
                    prevReadingId,
                    newReadingId,
                    alertSettings
                );
            } else if (ACTION_READ_FAILURE_AND_ALERT.equals(action)) {
                PreferencesWrapper preferences = PreferencesWrapper.get(getApplicationContext());
                long newReadingId = intent.getLongExtra(EXTRA_NEW_READING_ID, -1);
                AlertSettings alertSettings = new AlertSettings(preferences);
                handleActionReadFailureAndAlert(newReadingId, alertSettings);
            }
        }
    }

    /**
     * Detects light switches between the previous and new sensors reading, and alert if necessary.
     *
     * @param lightSwitchThreshold threshold determining if a light is on or off
     * @param prevReadingId        ID of the previous reading
     * @param newReadingId         ID of the new reading
     * @param alertSettings        alerting settings
     */
    private void handleActionReadNewDataAndAlert(
        double lightSwitchThreshold,
        long prevReadingId,
        long newReadingId,
        AlertSettings alertSettings
    ) {
        Log.i(LOG_TAG, "Will read the latest data and alert according to the settings");
        int minutesFromMidnight = now();
        ReadingWithLightDataPoints prevReading =
            db.dao().getReadingWithLightDataPoints(prevReadingId);
        ReadingWithLightDataPoints newReading =
            db.dao().getReadingWithLightDataPoints(newReadingId);
        Map<String, PrevNewPoint> comp = new HashMap<>();

        // Register all the previous points in the hashmap
        for (LightDataPoint point : prevReading.points) {
            comp.put(point.moteId, new PrevNewPoint(lightSwitchThreshold, point));
        }

        for (LightDataPoint point : newReading.points) {
            // If the new point has an associated previous point, we can compute a state evolution
            if (comp.containsKey(point.moteId)) {
                PrevNewPoint prevNewPoint = comp.get(point.moteId);
                prevNewPoint.setNewPoint(point);
                int state = prevNewPoint.getEvolution();
                if (state == SWITCHED_ON || state == PrevNewPoint.SWITCHED_OFF) {
                    // If a light switch occurred
                    handleSwitched(minutesFromMidnight, alertSettings, prevNewPoint);
                }
            }
        }
    }

    /**
     * Reads the reason of the reading failure and vibrates/sends a notification if necessary.
     *
     * @param newReadingId  ID of the failed reading
     * @param alertSettings alerting settings
     */
    private void handleActionReadFailureAndAlert(long newReadingId, AlertSettings alertSettings) {
        Log.i(LOG_TAG, "Will read the latest failure and alert according to the settings");
        int minutesFromMidnight = now();
        Reading reading = db.dao().getReading(newReadingId);

        String errorMessage = reading.errorMessage;
        // If there is an error http status code, improve the error message.
        if (reading.httpStatusCode != 0) {
            errorMessage = String.format("[%d] %s", reading.httpStatusCode, errorMessage);
        }

        if (alertSettings.shouldVibrate(minutesFromMidnight)) {
            vibrate();
        }
        if (alertSettings.shouldSendNotification(minutesFromMidnight)) {
            sendErrorNotification(errorMessage);
        }
    }

    /**
     * Returns the current number of minutes elapsed since midnight.
     *
     * @return the current number of minutes elapsed since midnight.
     */
    public static int now() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }

    /**
     * Vibrates, sends a notification, or sends an email to indicate a light switch if the alerting
     * settings allows it.
     *
     * @param minutesFromMidnight minutes elapsed since midnight
     * @param alertSettings       alerting settings
     * @param prevNewPoint        carries information about the light switch
     */
    private void handleSwitched(
        int minutesFromMidnight, AlertSettings alertSettings, PrevNewPoint prevNewPoint
    ) {
        LightDataPoint prevPoint = prevNewPoint.getPrevPoint();
        LightDataPoint newPoint = prevNewPoint.getNewPoint();

        Log.i(LOG_TAG, String.format("Light switch detected: %s", prevNewPoint.toString()));

        if (alertSettings.shouldVibrate(minutesFromMidnight)) {
            vibrate();
        }

        if (alertSettings.shouldSendNotification(minutesFromMidnight)) {
            sendSwitchNotification(newPoint.moteId, prevNewPoint.getEvolution(), newPoint.instant);
        }

        if (alertSettings.shouldSendEmail(minutesFromMidnight)) {
            sendSwitchEMail(newPoint.moteId,
                prevNewPoint.getEvolution(),
                prevPoint.instant,
                newPoint.instant,
                prevPoint.value,
                newPoint.value,
                prevNewPoint.getLightSwitchThreshold(),
                alertSettings.emailSender,
                alertSettings.emailRecipients.toArray(new String[0])
            );
        }
    }

    /**
     * Triggers a vibration on the device.
     */
    private void vibrate() {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            Log.i(LOG_TAG, "Vibration successful");
        } catch (Exception e) {
            Log.e(LOG_TAG, String.format("Cannot vibrate: %s", e));
        }
    }

    /**
     * Displays a notification on the device to indicate a reading error.
     *
     * @param errorMessage the error message
     */
    private void sendErrorNotification(String errorMessage) {
        try {
            Notification notification = new NotificationCompat.Builder(getApplicationContext(),
                buildAndGetChannel()
            ).setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Sensors reading error")
                .setContentText(String.format("Unable to read the sensors: %s", errorMessage))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
            notificationManager.notify(randomNotificationId(), notification);
            Log.i(LOG_TAG, "Error notification successfully sent");
        } catch (Exception e) {
            Log.e(LOG_TAG, String.format("Unable to send error notification: %s", e));
        }
    }

    /**
     * Displays a notification on the device to indicate a light switch
     *
     * @param moteId     ID of the mote which detected the light switch
     * @param evolution  state evolution detected on this mote (SWITCHED_ON or SWITCHED_OFF
     *                   constant)
     * @param newInstant instant when the light switch has been detected
     */
    private void sendSwitchNotification(String moteId, int evolution, OffsetDateTime newInstant) {
        String moteName = db.dao().getAlias(moteId);
        // Computes a short and long name depending on whether this mote has an alias or not
        String moteShortName = moteName == null ? moteId : moteName;
        String moteLongName =
            moteName == null ? moteId : String.format(String.format(moteName, moteId));

        try {
            Notification notification = new NotificationCompat.Builder(getApplicationContext(),
                buildAndGetChannel()
            ).setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(String.format("%s: Light switched %s",
                    moteShortName,
                    evolution == SWITCHED_ON ? "on" : "off"
                ))
                .setWhen(newInstant.toEpochSecond() * 1000)
                .setContentText(String.format("Light switched %s in room %s at %02d:%02d",
                    evolution == SWITCHED_ON ? "on" : "off",
                    moteLongName,
                    newInstant.getHour(),
                    newInstant.getMinute()
                ))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
            notificationManager.notify(randomNotificationId(), notification);
            Log.i(LOG_TAG, "Switch notification successfully sent");
        } catch (Exception e) {
            Log.e(LOG_TAG, String.format("Unable to send switch notification: %s", e));
        }
    }

    /**
     * Sends an email to indicate a light switch.
     *
     * @param moteId               ID of the mote which detected the light switch
     * @param evolution            state evolution detected on this mote (SWITCHED_ON or
     *                             SWITCHED_OFF constant)
     * @param prevInstant          instant of the previous light data point
     * @param newInstant           instant of the new light data point
     * @param prevValue            previous value read by this mote sensors
     * @param newValue             new value read by this mote sensors
     * @param lightSwitchThreshold threshold determining if a light is on or off
     * @param sender               email address of the displayed sender
     * @param recipients           email addresses of the recipients
     */
    private void sendSwitchEMail(
        String moteId,
        int evolution,
        OffsetDateTime prevInstant,
        OffsetDateTime newInstant,
        double prevValue,
        double newValue,
        double lightSwitchThreshold,
        String sender,
        String[] recipients
    ) {
        String moteName = db.dao().getAlias(moteId);
        String subject = String.format("[SensorAlert] %s: Light switched %s",
            moteName == null ? moteId : moteName,
            evolution == SWITCHED_ON ? "on" : "off"
        );
        String body = String.format(
            "<h1 id=\"sensoralert-report-light-switched-s\">SensorAlert report : Light switched %s</h1>\n" +
            "<ul>\n" + "<li><strong>Where</strong> : %s</li>\n" +
            "<li><strong>When (approx.)</strong> : %s</li>\n" + "</ul>\n" +
            "<h2 id=\"advanced-data\">Advanced data</h2>\n" + "<ul>\n" +
            "<li><strong>Previous reading</strong> : %.2f at %s</li>\n" +
            "<li><strong>Current reading</strong> : %.2f at %s</li>\n" +
            "<li><strong>Light threshold used</strong> : %.2f</li>\n" + "</ul>\n" + "<hr>\n" +
            "<p><em>You can customize or disable email alerts in your SensorAlert app settings</em></p>\n",
            evolution == SWITCHED_ON ? "on" : "off",
            moteName == null ?
            String.format("detected by mote %s", moteId) :
            String.format("%s (detected by mote %s)", moteName, moteId),
            newInstant.format(DateTimeFormatter.RFC_1123_DATE_TIME),
            prevValue,
            prevInstant.format(DateTimeFormatter.RFC_1123_DATE_TIME),
            newValue,
            newInstant.format(DateTimeFormatter.RFC_1123_DATE_TIME),
            lightSwitchThreshold
        );
        Multipart multipart;
        try {
            multipart = EmailHandling.makeMultipart(EmailHandling.makeHtmlBodyPart(body));
        } catch (MessagingException e) {
            Log.e(LOG_TAG,
                String.format("Unable to create the email body content: %s", e.toString())
            );
            return;
        }
        try {
            EmailHandling.sendEmail(emailSession, sender, recipients, subject, multipart);
            Log.i(LOG_TAG,
                String.format("Email successfully sent to %s", String.join(", ", recipients))
            );
        } catch (Exception e) {
            Log.e(LOG_TAG,
                String.format("Unable to send the light switch email: %s", e.toString())
            );
        }
    }

    /**
     * Creates a notification channel and returns the corresponding access key.
     *
     * @return the created channel key
     */
    private String buildAndGetChannel() {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
            "Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.enableLights(true);
        channel.enableVibration(false);
        notificationManager.createNotificationChannel(channel);
        return NOTIFICATION_CHANNEL_ID;
    }

    /**
     * Returns a random ID for the notifications
     *
     * @return a random ID for the notifications
     */
    public static int randomNotificationId() {
        return new Random().nextInt() & Integer.MAX_VALUE;
    }
}

/**
 * Helper class used to contain all the preferences related to alerting.
 */
class AlertSettings {
    public final boolean vibrate;
    public final int vibrationBeginHour;
    public final int vibrationEndHour;
    public final boolean sendNotification;
    public final int notificationBeginHour;
    public final int notificationEndHour;
    public final boolean sendEmail;
    public final int emailBeginHour;
    public final int emailEndHour;
    public final String emailSender;
    public final List<String> emailRecipients;

    public AlertSettings(PreferencesWrapper preferences) {
        vibrate = preferences.getBoolean("vibrate");
        vibrationBeginHour = preferences.getInt("vibration_begin_hour");
        vibrationEndHour = preferences.getInt("vibration_end_hour");
        sendNotification = preferences.getBoolean("send_notification");
        notificationBeginHour = preferences.getInt("notification_begin_hour");
        notificationEndHour = preferences.getInt("notification_end_hour");
        emailBeginHour = preferences.getInt("email_begin_hour");
        emailEndHour = preferences.getInt("email_end_hour");
        emailSender = preferences.getString("smtp_username");
        String recipientsString = preferences.getString("email_recipients");
        String[] recipients = recipientsString.split(";");
        emailRecipients = new ArrayList<>();
        for (String recipient : recipients) {
            if (EmailHandling.isValidEmail(recipient)) {
                emailRecipients.add(recipient);
            }
        }
        sendEmail = preferences.getBoolean("send_email") && !emailRecipients.isEmpty();
    }

    /**
     * Returns true iif a switch email should be sent at this time.
     *
     * @param minutesFromMidnight minutes elapsed since midnight
     * @return true iif a switch email should be sent at this time
     */
    public boolean shouldSendEmail(int minutesFromMidnight) {
        return shouldAlert(sendEmail, emailBeginHour, emailEndHour, minutesFromMidnight);
    }

    /**
     * Returns true if an alerting system should be triggered given the begin hour, end hour,
     * activation status and the current time.
     *
     * @param activated           is the alerting system activated?
     * @param beginHour           begin hour set in the preferences
     * @param endHour             end hour set in the preferences
     * @param minutesFromMidnight minutes elapsed since midnight
     * @return whether the alerting system should be triggered or not
     */
    private boolean shouldAlert(
        boolean activated, int beginHour, int endHour, int minutesFromMidnight
    ) {
        if (!activated) {
            return false;
        }
        if (beginHour == endHour) {
            // Special case for always
            return true;
        }
        if (beginHour < endHour) {
            // Two moments in the same day
            return minutesFromMidnight >= beginHour && minutesFromMidnight <= endHour;
        } else {
            // Two moments on two consecutive days
            if (minutesFromMidnight <= 23 * 60 + 59) {
                // evening
                return minutesFromMidnight >= beginHour;
            } else {
                // early morning
                return minutesFromMidnight <= endHour;
            }
        }
    }

    /**
     * Returns true iif a notification should be sent at this time.
     *
     * @param minutesFromMidnight minutes elapsed since midnight
     * @return true iif a notification should be sent at this time
     */
    public boolean shouldSendNotification(int minutesFromMidnight) {
        return shouldAlert(sendNotification,
            notificationBeginHour,
            notificationEndHour,
            minutesFromMidnight
        );
    }

    /**
     * Returns true iif a vibration should be triggered at this time.
     *
     * @param minutesFromMidnight minutes elapsed since midnight
     * @return true iif a vibration should be triggered at this time
     */
    public boolean shouldVibrate(int minutesFromMidnight) {
        return shouldAlert(vibrate, vibrationBeginHour, vibrationEndHour, minutesFromMidnight);
    }
}
