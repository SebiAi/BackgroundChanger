package com.sebiai.wallpaperchanger.fragments;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.sebiai.wallpaperchanger.R;
import com.sebiai.wallpaperchanger.dialogs.ConfigureAutomaticModeDialogFragment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat implements ConfigureAutomaticModeDialogFragment.OnConfigureAutomaticModeDialogDismissListener {
    private ActivityResultLauncher<Uri> uriActivityResultLauncher;
    private SharedPreferences sharedPreferences;

    private Preference preferenceWallpaperDir;
    private Preference preferenceIntervalTime;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setup();

        uriActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), result -> {
            if (result == null)
                return;

            // Make old not persistent any more
            if (getMyApplication(requireContext()).wallpaperDir != null)
                requireActivity().getContentResolver().releasePersistableUriPermission(getMyApplication(requireContext()).wallpaperDir,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Make persistent
            requireActivity().getContentResolver().takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Save globally and in preferences
            getMyApplication(requireContext()).wallpaperDir = result;
            sharedPreferences.edit().putString(getString(R.string.key_wallpaper_dir), result.toString()).apply();

            // Refresh preference
            setPreferenceSummary(preferenceWallpaperDir, String.format(getString(R.string.preference_wallpaper_dir_summary_string),
                    FileUtil.getFullPathFromTreeUri(result, requireContext())));
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Calendar calendar = Calendar.getInstance(Locale.GERMAN);
        calendar.set(1900, 0, 1, 12, 12, 0);

        Date date = calendar.getTime();
    }

    private void setup() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        preferenceWallpaperDir = findPreference(getString(R.string.key_wallpaper_dir));
        if (preferenceWallpaperDir != null) {
            preferenceWallpaperDir.setOnPreferenceClickListener(this::onPreferenceClick);
        }

        preferenceIntervalTime = findPreference(getString(R.string.key_interval_start_time));
        if (preferenceIntervalTime != null) {
            preferenceIntervalTime.setOnPreferenceClickListener(this::onPreferenceClick);
        }
        updatePreferenceValues();
    }

    private void setPreferenceSummary(Preference preference, String summary) {
        if (preference != null)
            preference.setSummary(summary);
    }

    private void updatePreferenceValues() {
        // Current Wallpaper Dir
        String wallpaperDir = "-";
        String stringUri = sharedPreferences.getString(getString(R.string.key_wallpaper_dir), null);

        Uri wallpaperDirUri = null;
        if (stringUri != null)
            wallpaperDirUri = Uri.parse(stringUri);

        if (wallpaperDirUri != null)
            wallpaperDir = FileUtil.getFullPathFromTreeUri(wallpaperDirUri, requireContext());

        setPreferenceSummary(preferenceWallpaperDir, String.format(getString(R.string.preference_wallpaper_dir_summary_string),
                wallpaperDir));
    }

    private boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "wallpaperDir":
                uriActivityResultLauncher.launch(Uri.parse("image/*"));
                return true;
            case "intervalStartTime":
                // Start dialog
                ConfigureAutomaticModeDialogFragment dialog = new ConfigureAutomaticModeDialogFragment();
                dialog.show(getParentFragmentManager(), null);
                return true;
        }
        return false;
    }

    @Override
    public void onConfigureAutomaticModeDialogDismissListener(Calendar intervalTime, Calendar startTime) {
        // TODO: Implement onConfigureAutomaticModeDialogDismissListener
        String intervalTimeString;
        String startTimeString;

        // Serialize to string
        try {
            intervalTimeString = toString(intervalTime);
            startTimeString = toString(startTime);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Save in sharedPreferences
        sharedPreferences.edit().
                putString(getString(R.string.key_interval_time), intervalTimeString).
                putString(getString(R.string.key_interval_start_time), startTimeString).
                apply();

        // Change summary of preference
        String preferenceSummary = String.format(getString(R.string.preference_interval_start_time_summary_string),
                intervalTime.get(Calendar.HOUR_OF_DAY), // TODO; Figure out how to extract more than just 0-23h
                intervalTime.get(Calendar.MINUTE),
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE));

        setPreferenceSummary(preferenceIntervalTime, preferenceSummary);
    }

    /** Write the object to a Base64 string. */
    private static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /** Read the object from Base64 string. */
    private static Object fromString( String s ) throws IOException ,
            ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }
}

