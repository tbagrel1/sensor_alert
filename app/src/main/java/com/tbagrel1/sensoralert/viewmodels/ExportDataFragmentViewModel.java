package com.tbagrel1.sensoralert.viewmodels;

import android.app.Application;
import android.app.DatePickerDialog.OnDateSetListener;
import android.text.InputType;
import android.view.View.OnClickListener;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.adapters.TextViewBindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tbagrel1.sensoralert.EmailHandling;
import com.tbagrel1.sensoralert.PreferencesWrapper;
import com.tbagrel1.sensoralert.fragments.ExportDataFragment;

import java.util.Calendar;

/**
 * View model for the export data fragment.
 */
public class ExportDataFragmentViewModel extends BaseObservable {
    /**
     * Enum listing the possible stages of export.
     */
    public enum Stage {
        PARAMETERS_NEED_ADJUSTING, PARAMETERS_OK, EXPORT_IN_PROGRESS, EXPORT_DONE
    }

    /**
     * Class used to represent a simple date (YYYY-MM-DD)
     */
    public static class DateFields implements Comparable<DateFields> {
        public final int year;
        public final int month;
        public final int day;

        public DateFields() {
            this.year = Calendar.getInstance().get(Calendar.YEAR);
            this.month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            this.day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        }

        public DateFields(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format("%04d-%02d-%02d", year, month, day);
        }

        @Override
        public int compareTo(DateFields that) {
            int cmp = year - that.year;
            if (cmp == 0) {
                cmp = month - that.month;
            }
            if (cmp == 0) {
                cmp = day - that.day;
            }
            return cmp;
        }
    }

    /**
     * Parameter container for the async date picker show action.
     */
    public static class DatePickerDialogShowBundle {
        public final OnDateSetListener callback;
        public final DateFields initDate;

        public DatePickerDialogShowBundle(OnDateSetListener callback, DateFields initDate) {
            this.callback = callback;
            this.initDate = initDate;
        }
    }

    /**
     * Parameter container for the export action.
     */
    public static class ExportBundle {
        public final ExportDataFragment.AsyncExportCallback callback;
        public final String recipientEmail;
        public final String beginDateString;
        public final String endDateString;

        public ExportBundle(
            ExportDataFragment.AsyncExportCallback callback,
            String recipientEmail,
            String beginDateString,
            String endDateString
        ) {
            this.callback = callback;
            this.recipientEmail = recipientEmail;
            this.beginDateString = beginDateString;
            this.endDateString = endDateString;
        }
    }

    private final MutableLiveData<DatePickerDialogShowBundle> datePickerDialogShowChannel =
        new MutableLiveData<>();
    private final MutableLiveData<ExportBundle> exportChannel = new MutableLiveData<>();
    private String recipientEmail;
    private DateFields beginDate;
    private DateFields endDate;
    private String status;
    private Stage stage;
    private final OnClickListener export = view -> {
        if (stage != Stage.PARAMETERS_OK) {
            return;
        }
        stage = Stage.EXPORT_IN_PROGRESS;
        status = "Export in progress...";
        notifyChange();

        exportChannel.setValue(new ExportBundle(success -> {
            stage = Stage.EXPORT_DONE;
            status = success ? "Export successful" : "Export error (see logs)";
            notifyChange();
        }, recipientEmail, getBeginDateString(), getEndDateString()));
    };
    private final OnDateSetListener editBeginDate = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            beginDate = new DateFields(year, month + 1, day);
            onParameterChanged();
        }
    };
    private final OnDateSetListener editEndDate = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            endDate = new DateFields(year, month + 1, day);
            onParameterChanged();
        }
    };
    private final OnClickListener selectBeginDate = view -> {
        if (stage != Stage.EXPORT_IN_PROGRESS) {
            DateFields initDate = beginDate == null ? new DateFields() : beginDate;
            datePickerDialogShowChannel.setValue(new DatePickerDialogShowBundle(editBeginDate,
                initDate
            ));
        }
    };
    private final OnClickListener selectEndDate = view -> {
        if (stage != Stage.EXPORT_IN_PROGRESS) {
            DateFields initDate = endDate == null ? new DateFields() : endDate;
            datePickerDialogShowChannel.setValue(new DatePickerDialogShowBundle(editEndDate,
                initDate
            ));
        }
    };
    private final TextViewBindingAdapter.OnTextChanged recipientEmailChanged =
        (recipientEmail, start, before, count) -> {
            this.recipientEmail = recipientEmail.toString();
            onParameterChanged();
        };

    public ExportDataFragmentViewModel(Application application) {

        String defaultRecipients = PreferencesWrapper.get(application.getApplicationContext())
            .getString("email_recipients");
        this.recipientEmail = defaultRecipients.split(",")[0];
        this.beginDate = null;
        this.endDate = null;
        onParameterChanged();
    }

    public void onParameterChanged() {
        String status = null;
        if (status == null && recipientEmail.isEmpty()) {
            status = "Please set a recipient email";
        }
        if (status == null && !EmailHandling.isValidEmail(recipientEmail)) {
            status = "The email address is not valid";
        }
        if (status == null && beginDate == null) {
            status = "Please set the begin date";
        }
        if (status == null && endDate == null) {
            status = "Please set the end date";
        }
        if (status == null && beginDate.compareTo(endDate) > 0) {
            status = "The end date cannot be before the begin date";
        }
        if (status == null) {
            this.status = "Ready to export";
            this.stage = Stage.PARAMETERS_OK;
        } else {
            this.status = status;
            this.stage = Stage.PARAMETERS_NEED_ADJUSTING;
        }
        notifyChange();
    }

    public LiveData<DatePickerDialogShowBundle> getDatePickerDialogShowChannel() {
        return datePickerDialogShowChannel;
    }

    public LiveData<ExportBundle> getExportChannel() {
        return exportChannel;
    }

    public int getInputTypeDisabled() {
        return InputType.TYPE_NULL;
    }

    public OnClickListener getExport() {
        return export;
    }

    public OnClickListener getSelectBeginDate() {
        return selectBeginDate;
    }

    public OnClickListener getSelectEndDate() {
        return selectEndDate;
    }

    public TextViewBindingAdapter.OnTextChanged getRecipientEmailChanged() {
        return recipientEmailChanged;
    }

    public int getRecipientEmailInputType() {
        if (stage == Stage.EXPORT_IN_PROGRESS) {
            return InputType.TYPE_NULL;
        } else {
            return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        }
    }

    public String getStatus() {
        return status;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getBeginDateString() {
        return beginDate == null ? "<None>" : beginDate.toString();
    }

    public String getEndDateString() {
        return endDate == null ? "<None>" : endDate.toString();
    }

    public boolean getExportButtonEnabled() {
        return stage == Stage.PARAMETERS_OK || stage == Stage.EXPORT_DONE;
    }
}
