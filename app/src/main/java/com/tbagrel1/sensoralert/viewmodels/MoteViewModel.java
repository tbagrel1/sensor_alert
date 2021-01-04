package com.tbagrel1.sensoralert.viewmodels;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.recyclerview.widget.DiffUtil;

import com.tbagrel1.sensoralert.PreferencesWrapper;
import com.tbagrel1.sensoralert.R;
import com.tbagrel1.sensoralert.dbmodels.LightDataPoint;
import com.tbagrel1.sensoralert.dbmodels.SensorAlertDatabase;

/**
 * View model for mote list items.
 */
public class MoteViewModel extends AndroidViewModel {
    public static final String LOG_TAG = "MoteViewModel";
    /**
     * Callback computing the difference between mote list items when the mote list changes.
     */
    public static final DiffUtil.ItemCallback<MoteViewModel> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<MoteViewModel>() {
            @Override
            public boolean areItemsTheSame(
                @NonNull MoteViewModel oldItem, @NonNull MoteViewModel newItem
            ) {
                return oldItem.getMoteId().equals(newItem.getMoteId());
            }

            @Override
            public boolean areContentsTheSame(
                @NonNull MoteViewModel oldItem, @NonNull MoteViewModel newItem
            ) {
                // We can safely return false here, because if the mote list is updated, mote values must have changed, and it would be impossible to implement a correct equals method on the livedata fields of this view model.
                return false;
            }
        };
    private final Application application;
    private final LightDataPoint lightDataPoint;
    private final LiveData<String> moteName;
    private final LiveData<String> moteDisplayedName;
    private final LiveData<Integer> moteIdVisibility;

    public MoteViewModel(@NonNull Application application, LightDataPoint lightDataPoint) {
        super(application);
        this.application = application;
        SensorAlertDatabase db =
            SensorAlertDatabase.getInstance(application.getApplicationContext());
        this.lightDataPoint = lightDataPoint;
        moteName = db.dao().liveAlias(lightDataPoint.moteId);
        moteDisplayedName =
            Transformations.map(moteName, name -> (name == null ? lightDataPoint.moteId : name));
        moteIdVisibility =
            Transformations.map(moteName, name -> (name == null ? View.INVISIBLE : View.VISIBLE));
    }

    public LiveData<String> getMoteName() {
        return moteName;
    }

    public LiveData<String> getMoteDisplayedName() {
        return moteDisplayedName;
    }

    public LiveData<Integer> getMoteIdVisibility() {
        return moteIdVisibility;
    }

    public String getMoteId() {
        return lightDataPoint.moteId;
    }

    public String getValueString() {
        return String.format("%.2f", lightDataPoint.value);
    }

    public long getUpdatedAt() {
        return lightDataPoint.instant.toEpochSecond() * 1000;
    }

    public int getLightIconResource() {
        return isOn() ? R.drawable.light_on : R.drawable.light_off;
    }

    public boolean isOn() {
        return lightDataPoint.value >= getLightSwitchThreshold();
    }

    public double getLightSwitchThreshold() {
        PreferencesWrapper preferences =
            PreferencesWrapper.get(application.getApplicationContext());
        double lightSwitchThreshold = preferences.getInt("light_switch_threshold");
        return lightSwitchThreshold;
    }
}
