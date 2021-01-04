package com.tbagrel1.sensoralert.viewmodels;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.adapters.TextViewBindingAdapter;

import com.github.curioustechizen.ago.RelativeTimeTextView;

/**
 * Binding adapters for the viewmodel/XML android databinding.
 */
public class Adapters {
    @BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    @BindingAdapter("android:textColor")
    public static void setTextColorResource(TextView textView, int resource) {
        textView.setTextColor(resource);
    }

    @BindingAdapter("rttv:reference_time")
    public static void setReferenceTime(RelativeTimeTextView view, long time) {
        view.setReferenceTime(time);
    }

    @BindingAdapter("android:onTextChanged")
    public static void setListener(
        TextView view, TextViewBindingAdapter.OnTextChanged onTextChanged
    ) {
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                CharSequence charSequence, int start, int count, int after
            ) {
            }

            @Override
            public void onTextChanged(
                CharSequence charSequence, int start, int count, int after
            ) {
                onTextChanged.onTextChanged(charSequence, start, count, after);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}
