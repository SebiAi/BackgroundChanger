package com.sebiai.wallpaperchanger.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.sebiai.wallpaperchanger.R;

import java.util.Date;
import java.util.Objects;

public class ConfigureAutomaticModeDialogFragment extends DialogFragment {
    OnConfigureAutomaticModeDialogDismissListener mCallback;

    private NumberPicker numberPickerIntervalHours;
    private NumberPicker numberPickerIntervalMinutes;
    private TimePicker timePickerStartTime;

    boolean isPositive = false;

    public interface OnConfigureAutomaticModeDialogDismissListener {
        void onConfigureAutomaticModeDialogDismissListener(Date intervalTime, Date startTime);
    }

    public ConfigureAutomaticModeDialogFragment() {
        // Empty constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        NavHostFragment navHostFragment = ((NavHostFragment) Objects.requireNonNull(requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)));
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);

        try {
            mCallback = (OnConfigureAutomaticModeDialogDismissListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment
                    + " must implement OnDialogDismissListener");
        }
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(inflater.inflate(R.layout.dialog_configure_automatic_mode, null)).
                setPositiveButton(requireContext().getString(R.string.ok_string), (dialog, which) -> {
                    isPositive = true;
                    dialog.dismiss();
                }).
                setNegativeButton(requireContext().getString(R.string.cancel_string), (dialog, id) -> dialog.dismiss());
        return builder.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // TODO: [FIX ME] Does not get called somehow?? Why?

        // Get views
        numberPickerIntervalHours = view.findViewById(R.id.number_picker_interval_hours);
        numberPickerIntervalMinutes = view.findViewById(R.id.number_picker_interval_minutes);
        timePickerStartTime = view.findViewById(R.id.time_picker_start_time);

        // Set min/max values
        numberPickerIntervalHours.setMinValue(0);
        numberPickerIntervalHours.setMaxValue(100);
        numberPickerIntervalHours.setValue(0);
        numberPickerIntervalMinutes.setMinValue(15); // Min time is 15 min
        numberPickerIntervalMinutes.setMinValue(59);

        // Set on change listeners
        numberPickerIntervalHours.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (newVal == 0) {
                // mins should at least be 15
                if (numberPickerIntervalMinutes.getValue() < 15)
                    numberPickerIntervalMinutes.setValue(15);
                numberPickerIntervalMinutes.setMinValue(15);
            } else {
                numberPickerIntervalMinutes.setMinValue(0);
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (isPositive) {
            Date intervalTime = null;
            Date startTime = null;

            // TODO: Implement mCallback for OnConfigureAutomaticModeDialogDismissListener

            mCallback.onConfigureAutomaticModeDialogDismissListener(intervalTime, startTime);
        }
        super.onDismiss(dialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
