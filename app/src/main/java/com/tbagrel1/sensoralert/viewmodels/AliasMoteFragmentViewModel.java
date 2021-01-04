package com.tbagrel1.sensoralert.viewmodels;

import android.view.View.OnClickListener;

import androidx.databinding.BaseObservable;
import androidx.databinding.adapters.TextViewBindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tbagrel1.sensoralert.fragments.AliasMoteFragment;

/**
 * Viewmodel for the alias mote fragment
 */
public class AliasMoteFragmentViewModel extends BaseObservable {
    /**
     * Parameter container for the async alias mote task
     */
    public static class AliasMoteBundle {
        public final AliasMoteFragment.AsyncAliasMoteCallback callback;
        public final String moteId;
        public final String moteName;

        public AliasMoteBundle(
            AliasMoteFragment.AsyncAliasMoteCallback callback, String moteId, String moteName
        ) {
            this.callback = callback;
            this.moteId = moteId;
            this.moteName = moteName;
        }
    }

    private final MutableLiveData<AliasMoteBundle> aliasMoteChannel = new MutableLiveData<>();
    private String persistedName;
    private String name;
    private final TextViewBindingAdapter.OnTextChanged onNameChanged =
        (newName, start, before, count) -> {
            name = newName.toString();
            notifyChange();
        };
    private boolean backgroundProcessing;
    private String status;
    private final AliasMoteFragment.AsyncAliasMoteCallback applyCallback = () -> {
        backgroundProcessing = false;
        status = "New mote name applied";
        persistedName = name;
        notifyChange();
    };
    private final AliasMoteFragment.AsyncAliasMoteCallback clearCallback = () -> {
        backgroundProcessing = false;
        status = "Custom mote name cleared";
        name = "";
        persistedName = null;
        notifyChange();
    };
    private String moteId;
    private final OnClickListener onApplyClick = view -> {
        backgroundProcessing = true;
        status = "Applying the new mote name...";
        aliasMoteChannel.setValue(new AliasMoteBundle(applyCallback, moteId, name));
        notifyChange();
    };
    private final OnClickListener onClearClick = view -> {
        backgroundProcessing = true;
        status = "Clearing the custom mote name...";
        aliasMoteChannel.setValue(new AliasMoteBundle(clearCallback, moteId, null));
        notifyChange();
    };

    public AliasMoteFragmentViewModel(String moteId, String moteName) {
        this.persistedName = moteName;
        this.name = moteName == null ? "" : moteName;
        this.backgroundProcessing = false;
        this.status = "";
        this.moteId = moteId;
    }

    public String getName() {
        return name;
    }

    public TextViewBindingAdapter.OnTextChanged getOnNameChanged() {
        return onNameChanged;
    }

    public OnClickListener getOnApplyClick() {
        return onApplyClick;
    }

    public OnClickListener getOnClearClick() {
        return onClearClick;
    }

    public boolean getClearEnabled() {
        return !backgroundProcessing && persistedName != null;
    }

    public boolean getApplyEnabled() {
        return !backgroundProcessing && !name.equals(persistedName) && !name.isEmpty();
    }

    public LiveData<AliasMoteBundle> getAliasMoteChannel() {
        return aliasMoteChannel;
    }

    public String getStatus() {
        return status;
    }
}
