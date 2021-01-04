package com.tbagrel1.sensoralert.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.tbagrel1.sensoralert.MainActivity;
import com.tbagrel1.sensoralert.R;
import com.tbagrel1.sensoralert.databinding.HomeFragmentViewBinding;
import com.tbagrel1.sensoralert.dbmodels.ReadingWithLightDataPoints;
import com.tbagrel1.sensoralert.dbmodels.SensorAlertDatabase;
import com.tbagrel1.sensoralert.viewmodels.HomeFragmentViewModel;
import com.tbagrel1.sensoralert.viewmodels.MoteListAdapter;

/**
 * Home fragment, displaying the list of motes.
 */
public class HomeFragment extends Fragment implements DisplayedAwareFragment {
    public static final String LOG_TAG = "HomeFragment";

    public HomeFragment() {
        super(R.layout.home_fragment_view);
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        Log.i(LOG_TAG, "HomeFragment starting up...");
        setHasOptionsMenu(true);
        HomeFragmentViewBinding binding =
            DataBindingUtil.inflate(inflater, R.layout.home_fragment_view, container, false);
        SensorAlertDatabase db =
            SensorAlertDatabase.getInstance(getActivity().getApplicationContext());
        HomeFragmentViewModel viewModel = new HomeFragmentViewModel(getActivity().getApplication());
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        View view = binding.getRoot();
        RecyclerView listView = view.findViewById(R.id.mote_list);
        MoteListAdapter adapter =
            new MoteListAdapter((MainActivity) getActivity(), getViewLifecycleOwner());
        listView.setAdapter(adapter);

        // Observe the latest reading, to show a toast if a reading fails.
        db.dao()
            .liveLatestReadingWithLightDataPoints()
            .observe(getViewLifecycleOwner(), this::showToastIfNeeded);
        // Observe the light data point list, to update the list adapter with the new list when it changes.
        viewModel.getLightDataPoints().observe(getViewLifecycleOwner(), adapter::submitList);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        FragmentUtils.hideKeyboard(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_export_data:
                ((MainActivity) getActivity()).pushExportDataFragment();
                return true;
            case R.id.action_preferences:
                ((MainActivity) getActivity()).pushPreferencesFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Displays a toast if the latest reading failed.
     *
     * @param latestReadingWithLightDataPoints the latest reading
     */
    private void showToastIfNeeded(ReadingWithLightDataPoints latestReadingWithLightDataPoints) {
        if (latestReadingWithLightDataPoints == null) {
            Log.i(LOG_TAG, "No need to show a toast because there is no latest reading yet");
            return;
        }
        if (!latestReadingWithLightDataPoints.reading.isSuccess) {
            // Improving the error message if there's an http error code available
            if (latestReadingWithLightDataPoints.reading.httpStatusCode != 0) {
                latestReadingWithLightDataPoints.reading.errorMessage = String.format(
                    "[%d] %s",
                    latestReadingWithLightDataPoints.reading.httpStatusCode,
                    latestReadingWithLightDataPoints.reading.errorMessage
                );
            }
            Log.i(LOG_TAG, "Reading error detected by MainActivity, showing toast...");
            Toast.makeText(
                getActivity().getApplicationContext(),
                latestReadingWithLightDataPoints.reading.errorMessage,
                Toast.LENGTH_LONG
            ).show();
        }
    }

    /**
     * Set the title when this fragment is displayed.
     */
    @Override
    public void onDisplay() {
        Log.i(LOG_TAG, "Displayed");
        ((MainActivity) getActivity()).setTitleBar("SensorAlert", false);
    }
}
