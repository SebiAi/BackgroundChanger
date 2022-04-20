package com.sebiai.wallpaperchanger.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.sebiai.wallpaperchanger.R;

import java.util.Objects;

public class QuestionDialogFragment extends DialogFragment {
    OnQuestionDialogDismissListener mCallback;

    private final String title;
    private final String message;
    private final String positiveButton;
    private final String negativeButton;

    private boolean isPositive = false;

    public interface OnQuestionDialogDismissListener {
        void onQuestionDialogDismissListener(boolean isPositive);
    }

    public QuestionDialogFragment(String title, String message, String positiveButton, String negativeButton) {
        this.title = title;
        this.message = message;
        this.positiveButton = positiveButton;
        this.negativeButton = negativeButton;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        NavHostFragment navHostFragment = ((NavHostFragment) Objects.requireNonNull(requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)));
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);

        try {
            mCallback = (OnQuestionDialogDismissListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment
                    + " must implement OnDialogDismissListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title).
                setMessage(message).
                setPositiveButton(positiveButton, (dialog, which) -> {
                    isPositive = true;
                    dialog.dismiss();
                }).
                setNegativeButton(negativeButton, (dialog, id) -> dialog.dismiss());
        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        mCallback.onQuestionDialogDismissListener(isPositive);
        super.onDismiss(dialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
