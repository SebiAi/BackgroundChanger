package com.sebiai.wallpaperchanger;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.widget.Toast;

public class InfoFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
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
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setup() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        preferenceCurrentPictureName = findPreference(getString(R.string.key_current_picture));
        if (preferenceCurrentPictureName != null) {
            preferenceCurrentPictureName.setOnPreferenceClickListener(this::onPreferenceClick);
        }

        preferenceAmountPictures = findPreference(getString(R.string.key_amount_picture));
        if (preferenceAmountPictures != null) {
            preferenceAmountPictures.setOnPreferenceClickListener(this::onPreferenceClick);
        }
        int amountPictures = MyFileHandler.getFiles(requireContext(), getMyApplication(requireContext()).wallpaperDir).size();
        sharedPreferences.edit().putInt(getString(R.string.key_amount_picture), amountPictures).apply();

        preferenceAmountChanges = findPreference(getString(R.string.key_amount_changes));
        if (preferenceAmountChanges != null) {
            preferenceAmountChanges.setOnPreferenceClickListener(this::onPreferenceClick);
        }
        updatePreferenceValues();
    }

    private void updatePreferenceValues() {
        if (preferenceCurrentPictureName != null) {
            preferenceCurrentPictureName.setTitle(String.format(getString(R.string.preference_current_picture_string),
                    sharedPreferences.getString(preferenceCurrentPictureName.getKey(), "-")));
        }

        if (preferenceAmountPictures != null) {
            preferenceAmountPictures.setTitle(String.format(getString(R.string.preference_amount_picture_string),
                    sharedPreferences.getInt(preferenceAmountPictures.getKey(), 0)));
        }

        if (preferenceAmountChanges != null) {
            int amountChangesAutomatic = sharedPreferences.getInt(getString(R.string.key_amount_changes_automatic), 0);
            int amountChangesManual = sharedPreferences.getInt(getString(R.string.key_amount_changes_manual), 0);
            int amountChanges = amountChangesAutomatic + amountChangesManual;

            preferenceAmountChanges.setTitle(String.format(getString(R.string.preference_amount_changes_string), amountChanges));
            preferenceAmountChanges.setSummary(String.format(getString(R.string.preference_amount_changes_summary_string),
                    amountChangesAutomatic,
                    amountChangesManual));
        }
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO: [Optional] Could be optimized to only refresh the shared preference that actually changed
        updatePreferenceValues();
    }
}