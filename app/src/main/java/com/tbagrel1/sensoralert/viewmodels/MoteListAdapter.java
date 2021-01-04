package com.tbagrel1.sensoralert.viewmodels;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tbagrel1.sensoralert.R;
import com.tbagrel1.sensoralert.databinding.MoteViewBinding;

/**
 * List adapter allowing to display a list of mote in a recycler view.
 */
public class MoteListAdapter extends ListAdapter<MoteViewModel, MoteListAdapter.MoteViewHolder> {
    /**
     * Interface used to trigger alias and detail action when a mote list item is clicked
     */
    public interface ClickListener {
        void pushAliasMoteFragment(String moteId, String moteName);

        void pushMoteChartFragment(String moteId);
    }

    public class MoteViewHolder extends RecyclerView.ViewHolder {
        // https://medium.com/androiddevelopers/android-data-binding-recyclerview-db7c40d9f0e4
        private final MoteViewBinding binding;

        public MoteViewHolder(MoteViewBinding binding) {
            super(binding.getRoot());
            // Register listener on the alias button for the alias action
            ImageView imageView = itemView.findViewById(R.id.alias_button);
            imageView.setOnClickListener(view -> {
                MoteViewModel mote = getItem(getAdapterPosition());
                clickListener.pushAliasMoteFragment(mote.getMoteId(),
                    mote.getMoteName().getValue()
                );
            });
            // Register listener on the rest of the mote list item for the detail action
            ConstraintLayout layout = itemView.findViewById(R.id.mote);
            layout.setOnClickListener(view -> {
                clickListener.pushMoteChartFragment(getItem(getAdapterPosition()).getMoteId());
            });
            this.binding = binding;
        }

        /**
         * Binds the mote view model to the mote list item view.
         *
         * @param moteViewModel  the mote view model to bind
         * @param lifecycleOwner lifecycle owner for livedata observation
         */
        public void bind(MoteViewModel moteViewModel, LifecycleOwner lifecycleOwner) {
            binding.setViewModel(moteViewModel);
            binding.setLifecycleOwner(lifecycleOwner);
            binding.executePendingBindings();
        }
    }

    private final ClickListener clickListener;
    // https://stackoverflow.com/questions/30284067/handle-button-click-inside-a-row-in-recyclerview
    private final LifecycleOwner lifecycleOwner;

    // https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter

    public MoteListAdapter(ClickListener clickListener, LifecycleOwner lifecycleOwner) {
        super(MoteViewModel.DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.lifecycleOwner = lifecycleOwner;
    }

    @NonNull
    @Override
    public MoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // Creates a mote view holder from the MoteViewBinding (which represents a bindable mote view associated to the mote view layout)
        MoteViewBinding moteViewBinding = MoteViewBinding.inflate(layoutInflater, parent, false);
        return new MoteViewHolder(moteViewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MoteViewHolder holder, int position) {
        MoteViewModel moteViewModel = getItem(position);
        holder.bind(moteViewModel, lifecycleOwner);
    }
}
