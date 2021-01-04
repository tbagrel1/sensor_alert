package com.tbagrel1.sensoralert.viewmodels;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.tbagrel1.sensoralert.R;
import com.tbagrel1.sensoralert.dbmodels.ReadingWithLightDataPoints;
import com.tbagrel1.sensoralert.dbmodels.SensorAlertDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View model for the home fragment.
 */
public class HomeFragmentViewModel extends AndroidViewModel {
    public static final String LOG_TAG = "HomeFragmentViewModel";

    private final LiveData<Integer> noDataTextVisibility;
    private final LiveData<Long> updatedAt;
    private final LiveData<String> updateStatus;
    private final LiveData<Integer> statusColor;

    private final LiveData<List<MoteViewModel>> lightDataPoints;

    public HomeFragmentViewModel(@NonNull Application application) {
        super(application);
        SensorAlertDatabase db =
            SensorAlertDatabase.getInstance(application.getApplicationContext());

        LiveData<ReadingWithLightDataPoints> latestReadingLive =
            db.dao().liveLatestReadingWithLightDataPoints();
        LiveData<ReadingWithLightDataPoints> latestSuccessfulReadingLive =
            db.dao().liveLatestSuccessfulReadingWithLightDataPoints();

        this.noDataTextVisibility = Transformations.map(
            latestSuccessfulReadingLive,
            readingWithLightDataPoints -> readingWithLightDataPoints == null ?
                                          View.VISIBLE :
                                          View.GONE
        );
        this.updatedAt = Transformations.map(
            latestReadingLive,
            readingWithLightDataPoints -> readingWithLightDataPoints == null ?
                                          Calendar.getInstance().getTime().getTime() :
                                          readingWithLightDataPoints.reading.instant.toEpochSecond() *
                                          1000
        );
        this.updateStatus = Transformations.map(
            latestReadingLive,
            readingWithLightDataPoints ->
                readingWithLightDataPoints == null || readingWithLightDataPoints.reading.isSuccess ?
                "Success" :
                "Failure"
        );
        this.statusColor = Transformations.map(
            latestReadingLive,
            readingWithLightDataPoints -> application.getColor(
                readingWithLightDataPoints == null || readingWithLightDataPoints.reading.isSuccess ?
                R.color.success :
                R.color.failure)
        );

        this.lightDataPoints =
            Transformations.map(latestSuccessfulReadingLive, readingWithLightDataPoints -> {
                if (readingWithLightDataPoints == null) {
                    return new ArrayList<>();
                }
                return readingWithLightDataPoints.points.stream()
                    .map(lightDataPoint -> new MoteViewModel(getApplication(), lightDataPoint))
                    .sorted((mote1, mote2) -> {
                        int comp = (mote2.isOn() ? 1 : 0) - (mote1.isOn() ? 1 : 0);
                        if (comp == 0) {
                            comp = mote1.getMoteId().compareTo(mote2.getMoteId());
                        }
                        return comp;
                    })
                    .collect(Collectors.toList());
            });
    }

    public LiveData<Integer> getNoDataTextVisibility() {
        return noDataTextVisibility;
    }

    public LiveData<List<MoteViewModel>> getLightDataPoints() {
        return lightDataPoints;
    }

    public LiveData<Long> getUpdatedAt() {
        return updatedAt;
    }

    public LiveData<String> getUpdateStatus() {
        return updateStatus;
    }

    public LiveData<Integer> getStatusColor() {
        return statusColor;
    }
}
