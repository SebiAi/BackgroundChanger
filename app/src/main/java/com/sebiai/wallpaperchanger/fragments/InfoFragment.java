package com.sebiai.wallpaperchanger.fragments;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.widget.Toast;

import com.sebiai.wallpaperchanger.MyFileHandler;
import com.sebiai.wallpaperchanger.R;

public class InfoFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPreferences;

    private Preference preferenceCurrentPictureName;
    private Preference preferenceAmountPictures;
    private Preference preferenceAmountChanges;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.info_preferences, rootKey);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setup();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register Listeners
        sharedPreferences.registerOnSharedPreferenceChangeListener(this::onSharedPreferenceChangeListener);
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
                putInt(getString(R.string.key_amount_pictures), MyFileHandler.getFiles(requireContext(), getMyApplication(requireContext()).wallpaperDir).size()).
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
            Uri lastWallpaperUri = Uri.parse(sharedPreferences.getString(getString(R.string.key_current_picture), null));
            getMyApplication(requireContext()).wallpaperFileName = MyFileHandler.getNameFromWallpaperUri(requireContext(), lastWallpaperUri);
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
            case "currentPictureName":
                // TODO: Implement me
                Toast.makeText(preference.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            case "amountPictures":
                // TODO: Implement me
                Toast.makeText(preference.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            case "amountChanges":
                // TODO: Ask with a dialog before resetting
                sharedPreferences.edit().
                        putInt(getString(R.string.key_amount_changes), 0).
                        putInt(getString(R.string.key_amount_changes_automatic), 0).
                        putInt(getString(R.string.key_amount_changes_manual), 0).
                        apply();
                return true;
        }
        return false;
    }
}