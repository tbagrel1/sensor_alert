package com.tbagrel1.sensoralert.fragments;

import android.app.Application;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.tbagrel1.sensoralert.EmailHandling;
import com.tbagrel1.sensoralert.MainActivity;
import com.tbagrel1.sensoralert.PreferencesWrapper;
import com.tbagrel1.sensoralert.R;
import com.tbagrel1.sensoralert.databinding.ExportDataFragmentViewBinding;
import com.tbagrel1.sensoralert.dbmodels.LightDataPoint;
import com.tbagrel1.sensoralert.dbmodels.SensorAlertDatabase;
import com.tbagrel1.sensoralert.viewmodels.ExportDataFragmentViewModel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.Collection;

import javax.mail.MessagingException;
import javax.mail.Multipart;

/**
 * Fragment used to export database data.
 */
public class ExportDataFragment extends Fragment implements DisplayedAwareFragment {
    public static final String LOG_TAG = "ExportDataFragment";

    /**
     * Async task callback interface
     */
    public interface AsyncExportCallback {
        void onComplete(boolean success);
    }

    /**
     * Async task used to fetch and export the database data. Async is required because of the
     * database call.
     */
    public static class AsyncExport extends AsyncTask<String, Process, Boolean> {
        private final AsyncExportCallback callback;
        private final WeakReference<Application> applicationRef;
        private final WeakReference<SensorAlertDatabase> dbRef;

        public AsyncExport(
            AsyncExportCallback callback, Application application, SensorAlertDatabase db
        ) {
            this.callback = callback;
            this.applicationRef = new WeakReference<>(application);
            this.dbRef = new WeakReference<>(db);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String[] recipients = {params[0]};
            String beginDateString = params[1];
            String endDateString = params[2];

            Application application = applicationRef.get();
            if (application == null) {
                Log.e(LOG_TAG, "Unable to get application behind weak reference");
                return false;
            }
            SensorAlertDatabase db = dbRef.get();
            if (db == null) {
                Log.e(LOG_TAG, "Unable to get db behind weak reference");
                return false;
            }

            String fileName =
                String.format("sensor_alert_export_%s_%s.json", beginDateString, endDateString);
            String subject =
                String.format("SensorAlert export from %s to %s", beginDateString, endDateString);
            Collection<LightDataPoint> data =
                db.dao().exportLightDataPoints(beginDateString, endDateString);
            Log.i(LOG_TAG, "Data successfully fetched for export");
            try {
                boolean first = true;
                OutputStreamWriter writer =
                    new OutputStreamWriter(application.getApplicationContext()
                        .openFileOutput(fileName, Context.MODE_PRIVATE));
                writer.write("[\n    ");
                for (LightDataPoint point : data) {
                    if (first) {
                        first = false;
                    } else {
                        writer.write(",\n    ");
                    }
                    writer.write(point.toSerialized());
                }
                writer.write("\n]");
                writer.close();
                Log.i(LOG_TAG, "JSON file successfully written");
            } catch (IOException e) {
                Log.e(LOG_TAG,
                    String.format("Unable to serialize the export data: %s", e.toString())
                );
                return false;
            }
            PreferencesWrapper preferences =
                PreferencesWrapper.get(application.getApplicationContext());
            String sender = preferences.getString("smtp_username");
            String path =
                application.getApplicationContext().getFilesDir().toString() + File.separator +
                fileName;
            Multipart multipart;
            try {
                multipart = EmailHandling.makeMultipart(EmailHandling.makeHtmlBodyPart(
                    subject + " (attached)."),
                    EmailHandling.makeAttachmentBodyPart(path, fileName)
                );
            } catch (MessagingException e) {
                Log.e(LOG_TAG,
                    String.format("Unable to create the email body content: %s", e.toString())
                );
                return false;
            }
            try {
                EmailHandling.sendEmail(EmailHandling.getEmailSession(preferences),
                    sender,
                    recipients,
                    subject,
                    multipart
                );
                Log.i(LOG_TAG,
                    String.format("Export email successfully sent to %s",
                        String.join(", ", recipients)
                    )
                );
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, String.format("Error sending the export email: %s", e.toString()));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) {
                callback.onComplete(success);
            }
        }
    }

    public ExportDataFragment() {
        super(R.layout.export_data_fragment_view);
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        Log.i(LOG_TAG, "ExportDataFragment starting up...");
        setHasOptionsMenu(true);
        ExportDataFragmentViewBinding binding =
            DataBindingUtil.inflate(inflater, R.layout.export_data_fragment_view, container, false);
        ExportDataFragmentViewModel viewModel =
            new ExportDataFragmentViewModel(getActivity().getApplication());
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        // Observe the datePickerDialogShow channel to open the datepicker dialog when the view model requests it.
        binding.getViewModel()
            .getDatePickerDialogShowChannel()
            .observe(getViewLifecycleOwner(), bundle -> {
                new DatePickerDialog(getContext(),
                    bundle.callback,
                    bundle.initDate.year,
                    bundle.initDate.month - 1,
                    bundle.initDate.day
                ).show();
            });
        // Observe the export channel to execute the async export task when the view model requests it.
        binding.getViewModel().getExportChannel().observe(getViewLifecycleOwner(), bundle -> {
            AsyncExport backgroundExport = new AsyncExport(bundle.callback,
                getActivity().getApplication(),
                SensorAlertDatabase.getInstance(getActivity().getApplicationContext())
            );
            backgroundExport.execute(bundle.recipientEmail,
                bundle.beginDateString,
                bundle.endDateString
            );
        });
        View view = binding.getRoot();
        return view;
    }

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
        ((MainActivity) getActivity()).setTitleBar("Export mote data", true);
    }
}
