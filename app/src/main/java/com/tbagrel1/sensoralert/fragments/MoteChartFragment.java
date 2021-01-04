package com.tbagrel1.sensoralert.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.annotations.PlotController;
import com.anychart.core.cartesian.series.Marker;
import com.anychart.enums.MarkerType;
import com.anychart.scales.DateTime;
import com.tbagrel1.sensoralert.MainActivity;
import com.tbagrel1.sensoralert.PreferencesWrapper;
import com.tbagrel1.sensoralert.R;
import com.tbagrel1.sensoralert.dbmodels.ChartPoint;
import com.tbagrel1.sensoralert.dbmodels.SensorAlertDatabase;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fragment displaying a mote detailed data.
 */
public class MoteChartFragment extends Fragment implements DisplayedAwareFragment {
    public static final String LOG_TAG = "MoteChartFragment";

    /**
     * Async callback interface.
     */
    public interface AsyncLoadChartDataCallback {
        void onLoaded(List<ChartPoint> data);
    }

    /**
     * Async task used to load chart data from the database. Async is required because of the
     * database call.
     */
    public static class AsyncLoadChartData extends AsyncTask<String, Process, List<ChartPoint>> {
        private final WeakReference<SensorAlertDatabase> dbRef;
        private final AsyncLoadChartDataCallback callback;

        public AsyncLoadChartData(AsyncLoadChartDataCallback callback, SensorAlertDatabase db) {
            this.callback = callback;
            dbRef = new WeakReference<>(db);
        }

        @Override
        protected List<ChartPoint> doInBackground(String... params) {
            String moteId = params[0];
            SensorAlertDatabase db = dbRef.get();
            if (db == null) {
                Log.e(LOG_TAG, "Unable to get db behind weak reference");
                return new ArrayList<>();
            }
            List<ChartPoint> points = db.dao().getChartPoints(moteId);
            Log.i(LOG_TAG, String.format("%d chart points successfully loaded!", points.size()));
            return points;
        }

        @Override
        protected void onPostExecute(List<ChartPoint> data) {
            callback.onLoaded(data);
        }
    }

    private String moteId;
    private SensorAlertDatabase db;

    public MoteChartFragment() {
        super(R.layout.mote_chart_fragment_view);
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        Log.i(LOG_TAG, "MoteChartFragment starting up...");
        setHasOptionsMenu(true);
        moteId = getArguments().getString("mote_id");
        db = SensorAlertDatabase.getInstance(getActivity().getApplicationContext());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView loadingLabel = view.findViewById(R.id.loading_label);
        AnyChartView anyChartView = view.findViewById(R.id.chart_container);

        // Run the background data loading
        AsyncLoadChartData backgroundLoadChartData = new AsyncLoadChartData(data -> {
            // When data is received, hide the loading label.
            loadingLabel.setVisibility(View.GONE);
            Cartesian chart = AnyChart.cartesian();
            chart.animation(true);
            // Plot the light data points
            List<DataEntry> anyChartData =
                data.stream().map(ChartPoint::toAnyChartPoint).collect(Collectors.toList());
            Marker marker = chart.marker(anyChartData);
            marker.type(MarkerType.CIRCLE).size(4);
            // Plot the threshold line
            PlotController controller = chart.annotations();
            double lightSwitchThreshold =
                PreferencesWrapper.get(getActivity().getApplicationContext())
                    .getInt("light_switch_threshold");
            controller.horizontalLine(String.format("{valueAnchor: %f}", lightSwitchThreshold));
            // Set the axis and grid format
            chart.xScale(DateTime.instantiate().maximumGap(0.25).minimumGap(0.25));
            chart.xGrid(0).enabled(true);
            chart.yGrid(0).enabled(true);
            chart.xAxis(0).title("Time");
            chart.yScale().minimum(0);
            chart.yScale().softMaximum(900);
            chart.yAxis(0).title("Light sensor value");
            // Show the chart
            anyChartView.setChart(chart);
            anyChartView.setVisibility(View.VISIBLE);
        }, SensorAlertDatabase.getInstance(getActivity().getApplicationContext()));
        backgroundLoadChartData.execute(moteId);
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
        MainActivity activity = (MainActivity) getActivity();
        activity.setTitleBar(String.format("Mote %s", moteId), true);
        db.dao()
            .liveAlias(moteId)
            .observe(this,
                name -> activity.setTitleBar(name == null ? String.format("Mote %s", moteId) : name,
                    true
                )
            );
    }
}
