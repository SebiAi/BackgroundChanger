package com.sebiai.wallpaperchanger.fragments;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.widget.Toast;

import com.sebiai.wallpaperchanger.utils.MyFileHandler;
import com.sebiai.wallpaperchanger.R;
import com.sebiai.wallpaperchanger.dialogs.QuestionDialogFragment;

public class InfoFragment extends PreferenceFragmentCompat implements QuestionDialogFragment.OnQuestionDialogDismissListener {
    private SharedPreferences sharedPreferences;

    private Preference preferenceCurrentPictureName;
    private Preference preferenceAmountPictures;
    private Preference preferenceAmountChanges;

    private Lifecycle.State lastState;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.info_preferences, rootKey);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setup();
        lastState = this.getLifecycle().getCurrentState();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register Listeners
        sharedPreferences.registerOnSharedPreferenceChangeListener(this::onSharedPreferenceChangeListener);

        if (lastState == Lifecycle.State.STARTED)
            updatePreferenceValues();
        lastState = this.getLifecycle().getCurrentState();
    }

    @Override
    public void onPause() {
        super.onPause();

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this::onSharedPreferenceChangeListener);
    }

    private void setup() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        preferenceCurrentPictureName = findPreference(getString(R.string.key_current_picture));
        if (preferenceCurrentPictureName != null) {
            preferenceCurrentPictureName.setOnPreferenceClickListener(this::onPreferenceClick);
        }

        preferenceAmountPictures = findPreference(getString(R.string.key_amount_pictures));
        if (preferenceAmountPictures != null) {
            preferenceAmountPictures.setOnPreferenceClickListener(this::onPreferenceClick);
        }
        sharedPreferences.edit().
                putInt(getString(R.string.key_amount_pictures), MyFileHandler.getFiles(requireContext(), MyFileHandler.getWallpaperDirUri(requireContext())).size()).
                apply();

        preferenceAmountChanges = findPreference(getString(R.string.key_amount_changes));
        if (preferenceAmountChanges != null) {
            preferenceAmountChanges.setOnPreferenceClickListener(this::onPreferenceClick);
        }

        updatePreferenceValues();
    }

    private void onSharedPreferenceChangeListener(SharedPreferences sharedPreferences, String key) {
        if (getContext() == null)
            return;
        if (key.equals(getString(R.string.key_current_picture))) {
            // Update name
            Uri currentWallpaperUri = MyFileHandler.getCurrentWallpaperUri(requireContext());
            getMyApplication(requireContext()).wallpaperFileName = MyFileHandler.getNameFromWallpaperUri(requireContext(), currentWallpaperUri);
            setPreferenceTitle(preferenceCurrentPictureName, String.format(getString(R.string.preference_current_picture_string),
                    getMyApplication(requireContext()).wallpaperFileName));
        } else if (key.equals(getString(R.string.key_amount_pictures))) {
            // Update amount of pictures
            setPreferenceTitle(preferenceAmountPictures, String.format(getString(R.string.preference_amount_picture_string),
                    sharedPreferences.getInt(preferenceAmountPictures.getKey(), 0)));
        } else if (key.equals(getString(R.string.key_amount_changes_manual)) ||
                key.equals(getString(R.string.key_amount_changes_automatic)) ||
                key.equals(getString(R.string.key_amount_changes))) {
            int amountChangesAutomatic = sharedPreferences.getInt(getString(R.string.key_amount_changes_automatic), 0);
            int amountChangesManual = sharedPreferences.getInt(getString(R.string.key_amount_changes_manual), 0);
            int amountChanges = amountChangesAutomatic + amountChangesManual;
            setPreferenceTitleAndSummary(preferenceAmountChanges,
                    String.format(getString(R.string.preference_amount_changes_string), amountChanges),
                    String.format(getString(R.string.preference_amount_changes_summary_string), amountChangesAutomatic, amountChangesManual));
        }
    }

    private void setPreferenceTitle(Preference preference, String title) {
        if (preference != null)
            preference.setTitle(title);
    }

    private void setPreferenceTitleAndSummary(Preference preference, String title, String summary) {
        if (preference != null) {
            preference.setTitle(title);
            preference.setSummary(summary);
        }
    }

    private void updatePreferenceValues() {
        // Current Wallpaper Name
        Uri currentWallpaperUri = MyFileHandler.getCurrentWallpaperUri(requireContext());
        if (getMyApplication(requireContext()).wallpaperFileName == null) {
            // Set from uri
            getMyApplication(requireContext()).wallpaperFileName = MyFileHandler.getNameFromWallpaperUri(requireContext(), currentWallpaperUri);
        }
        setPreferenceTitle(preferenceCurrentPictureName, String.format(getString(R.string.preference_current_picture_string),
                getMyApplication(requireContext()).wallpaperFileName));

        // Amount of Pictures
        setPreferenceTitle(preferenceAmountPictures, String.format(getString(R.string.preference_amount_picture_string),
                sharedPreferences.getInt(preferenceAmountPictures.getKey(), 0)));

        // Amount of Changes
        int amountChangesAutomatic = sharedPreferences.getInt(getString(R.string.key_amount_changes_automatic), 0);
        int amountChangesManual = sharedPreferences.getInt(getString(R.string.key_amount_changes_manual), 0);
        int amountChanges = amountChangesAutomatic + amountChangesManual;
        setPreferenceTitleAndSummary(preferenceAmountChanges,
                String.format(getString(R.string.preference_amount_changes_string), amountChanges),
                String.format(getString(R.string.preference_amount_changes_summary_string), amountChangesAutomatic, amountChangesManual));
    }

    private boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "currentPicture":
                String stringUri = sharedPreferences.getString(getString(R.string.key_current_picture), null);
                Uri lastWallpaperUri = null;
                if (stringUri != null)
                    lastWallpaperUri = Uri.parse(stringUri);

                if (lastWallpaperUri == null)
                    return true;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(lastWallpaperUri, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, getString(R.string.view_image_string)));
                return true;
            case "amountPictures":
                // TODO: Implement other activity which displays every picture
                Toast.makeText(preference.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            case "amountChanges":
                QuestionDialogFragment dialog = new QuestionDialogFragment(getString(R.string.question_title_reset_amount_changes),
                        getString(R.string.question_message_reset_amount_changes),
                        getString(R.string.yes_string),
                        getString(R.string.no_string));
                dialog.show(getParentFragmentManager(), null);
                return true;
        }
        return false;
    }

    @Override
    public void onQuestionDialogDismissListener(boolean isPositive) {
        if (!isPositive)
            return;

        sharedPreferences.edit().
                putInt(getString(R.string.key_amount_changes), 0).
                putInt(getString(R.string.key_amount_changes_automatic), 0).
                putInt(getString(R.string.key_amount_changes_manual), 0).
                apply();

        Toast.makeText(requireContext(), getString(R.string.confirmation_reset_amount_changes), Toast.LENGTH_SHORT).show();
    }
}