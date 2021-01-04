package com.tbagrel1.sensoralert;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.tbagrel1.sensoralert.fragments.AliasMoteFragment;
import com.tbagrel1.sensoralert.fragments.DisplayedAwareFragment;
import com.tbagrel1.sensoralert.fragments.ExportDataFragment;
import com.tbagrel1.sensoralert.fragments.HomeFragment;
import com.tbagrel1.sensoralert.fragments.MoteChartFragment;
import com.tbagrel1.sensoralert.fragments.PreferencesFragment;
import com.tbagrel1.sensoralert.services.ReadSensorsService;
import com.tbagrel1.sensoralert.viewmodels.MoteListAdapter;

/**
 * Main (and only one) activity of this application
 */
public class MainActivity extends AppCompatActivity implements MoteListAdapter.ClickListener {
    public static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "MainActivity starting up...");
        super.onCreate(savedInstanceState);

        // Set the activity layout
        setContentView(R.layout.main_activity_view);

        // Register a listener on the fragment manager to call the onDisplay() method when a
        // fragment come to the top of the fragment stack (that's to say, when it is shown to the
        // user). Other fragment lifecycle hooks, such as onResume(), onPause(), or onAttach() does
        // not always work well when fragments are restored from the backstack. This listener
        // requires that all the fragments used in this app implements the DisplayedAwareFragment
        // interface.
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            DisplayedAwareFragment fragment =
                (DisplayedAwareFragment) fragmentManager.findFragmentById(R.id.fragment_container);
            if (fragment != null) {
                fragment.onDisplay();
            }
        });

        // If there is no activity saved state, init the activity with the home fragment.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container, new HomeFragment())
                .addToBackStack(null)
                .commit();
        }
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "MainActivity being paused.");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(LOG_TAG, "MainActivity being resumed...");
        super.onResume();
        // This line should not be necessary, because the periodic execution of the service should
        // have
        // been programmed at boot time
        // But because calling startRefreshingSensors is a NO-OP if the periodic execution of the
        // service is already programmed,
        // we do it, just in case. This is especially useful when the app is installed the first
        // time or when developing this app, because the phone is not always rebooted.
        Log.i(
            LOG_TAG,
            "Programming the periodic execution of the ReadSensors service (just in case)"
        );
        ReadSensorsService.startRefreshingSensors(getApplicationContext());

        // When the app is launched, or whenever the app comes from the background, we trigger a
        // refresh.
        Log.i(LOG_TAG, "Call the ReadSensors service now");
        startService(ReadSensorsService.getReadSensorsIntent(getApplicationContext()));
    }

    /**
     * Launch the export data window
     */
    public void pushExportDataFragment() {
        getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragment_container, new ExportDataFragment())
            .addToBackStack(null)
            .commit();
    }

    /**
     * Launch the preference window
     */
    public void pushPreferencesFragment() {
        getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragment_container, new PreferencesFragment())
            .addToBackStack(null)
            .commit();
    }

    /**
     * Launch the alias mote window.
     *
     * @param moteId   ID of the mote to alias
     * @param moteName current alias of this mote
     */
    public void pushAliasMoteFragment(String moteId, String moteName) {
        Bundle bundle = new Bundle();
        bundle.putString("mote_id", moteId);
        bundle.putString("mote_name", moteName);
        AliasMoteFragment fragment = new AliasMoteFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();
    }

    /**
     * Launch the mote chart window.
     *
     * @param moteId ID of the mote to display the data of
     */
    public void pushMoteChartFragment(String moteId) {
        Bundle bundle = new Bundle();
        bundle.putString("mote_id", moteId);
        MoteChartFragment fragment = new MoteChartFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();
    }

    /**
     * Allows a fragment to set its own window title, and to set whether a back arrow will be
     * displayed in the top left corner (to go back to the previous fragment).
     *
     * @param title         the new window title
     * @param showBackArrow should a back arrow be displayed?
     */
    public void setTitleBar(String title, boolean showBackArrow) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(showBackArrow);
        actionBar.setDisplayHomeAsUpEnabled(showBackArrow);
        actionBar.setTitle(title);
    }

    /**
     * Implements the back arrow functionality to go back to the previous fragment.
     *
     * @param menuItem the activity menu
     * @return true if an action has been triggered
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
