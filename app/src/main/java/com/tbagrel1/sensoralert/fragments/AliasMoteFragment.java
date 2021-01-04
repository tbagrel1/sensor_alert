package com.tbagrel1.sensoralert.fragments;

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

import com.tbagrel1.sensoralert.MainActivity;
import com.tbagrel1.sensoralert.R;
import com.tbagrel1.sensoralert.databinding.AliasMoteFragmentViewBinding;
import com.tbagrel1.sensoralert.dbmodels.MoteAlias;
import com.tbagrel1.sensoralert.dbmodels.SensorAlertDatabase;
import com.tbagrel1.sensoralert.viewmodels.AliasMoteFragmentViewModel;

import java.lang.ref.WeakReference;

/**
 * Fragment for mote alias
 */
public class AliasMoteFragment extends Fragment implements DisplayedAwareFragment {
    public static final String LOG_TAG = "AliasMoteFragment";

    /**
     * Async task callback.
     */
    public interface AsyncAliasMoteCallback {
        void onComplete();
    }

    /**
     * Async task used to alias or unalias a mote. Required because of the database call.
     */
    public static class AsyncAliasMote extends AsyncTask<String, Process, String> {
        private final AsyncAliasMoteCallback callback;
        private final WeakReference<SensorAlertDatabase> dbRef;

        public AsyncAliasMote(AsyncAliasMoteCallback callback, SensorAlertDatabase db) {
            this.callback = callback;
            this.dbRef = new WeakReference<>(db);
        }

        @Override
        protected String doInBackground(String... params) {
            String moteId = params[0];
            String moteName = params[1];
            SensorAlertDatabase db = dbRef.get();
            if (db == null) {
                Log.e(LOG_TAG, "Unable to get db behind weak reference");
                return "Unable to access context";
            }
            if (moteName == null) {
                String prevMoteName = db.dao().getAlias(moteId);
                MoteAlias moteAlias = new MoteAlias(moteId, prevMoteName);
                db.dao().unalias(moteAlias);
                Log.i(LOG_TAG,
                    String.format("Mote alias successfully applied: %s", moteAlias.toString())
                );
            } else {
                MoteAlias moteAlias = new MoteAlias(moteId, moteName);
                db.dao().alias(moteAlias);
                Log.i(LOG_TAG,
                    String.format("Mote alias successfully removed: %s", moteAlias.toString())
                );
            }
            return moteName;
        }

        @Override
        protected void onPostExecute(String moteName) {
            callback.onComplete();
        }
    }

    private String moteId = null;
    private String moteName = null;

    public AliasMoteFragment() {
        super(R.layout.alias_mote_fragment_view);
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        Log.i(LOG_TAG, "AliasMoteFragment starting up...");
        setHasOptionsMenu(true);
        moteId = getArguments().getString("mote_id");
        moteName = getArguments().getString("mote_name");
        AliasMoteFragmentViewBinding binding =
            DataBindingUtil.inflate(inflater, R.layout.alias_mote_fragment_view, container, false);
        AliasMoteFragmentViewModel viewModel = new AliasMoteFragmentViewModel(moteId, moteName);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        // observe the aliasMoteChannel to trigger the async task when the view model requests it.
        binding.getViewModel().getAliasMoteChannel().observe(getViewLifecycleOwner(), bundle -> {
            AsyncAliasMote backgroundAliasMote = new AsyncAliasMote(bundle.callback,
                SensorAlertDatabase.getInstance(getActivity().getApplicationContext())
            );
            backgroundAliasMote.execute(bundle.moteId, bundle.moteName);
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
        ((MainActivity) getActivity()).setTitleBar(String.format("Set name for mote %s", moteId),
            true
        );
    }
}
